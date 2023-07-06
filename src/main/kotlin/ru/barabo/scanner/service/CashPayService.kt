package ru.barabo.scanner.service

import oracle.jdbc.OracleTypes
import org.slf4j.LoggerFactory
import ru.barabo.afina.AfinaOrm
import ru.barabo.afina.AfinaQuery
import ru.barabo.db.EditType
import ru.barabo.db.SessionException
import ru.barabo.db.SessionSetting
import ru.barabo.db.annotation.QuerySelect
import ru.barabo.db.service.StoreFilterService
import ru.barabo.gui.swing.ResourcesManager
import ru.barabo.gui.swing.processShowError
import ru.barabo.scanner.entity.CashPay
import java.awt.im.InputContext
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

object CashPayService : StoreFilterService<CashPay>(AfinaOrm, CashPay::class.java), QuerySelect, ScanEventListener {

    private val logger = LoggerFactory.getLogger(CashPayService::class.java)!!

    private val scannerInfo: MutableMap<String, String> = LinkedHashMap()

    private var scannerTemp: CashPay? = null

    private val fmsMap = LinkedHashMap<String, MutableList<String>>()

    fun getFmsByCode(code: String): List<String> = fmsMap[code] ?: emptyList()

    fun initFms() {
        val lines = ResourcesManager.readFms()

        var priorCode = ""

        for(line in lines) {
            val code = line.substringBefore('\t').trim()
            val name = line.substringAfter('\t').trim()

            if(code != priorCode) {
                priorCode = code

                val newFms = ArrayList<String>()
                newFms += name

                fmsMap[code] = newFms
            } else {
                val fmsList = fmsMap[code] ?: throw java.lang.Exception("Не найдена запись для кода =$code")
                fmsList += name
            }
        }
    }

    override fun selectQuery(): String = "{ ? = call od.PTKB_CASH.getCashPayList }"

    override fun processInsert(item: CashPay) {}

    override fun save(item: CashPay, sessionSetting: SessionSetting): CashPay {

        logger.error("save item=$item")

        item.checkFieldsBeforeSave()

        item.payerFio = item.payerFio.trim().uppercase(Locale.getDefault() )

        val type = orm.save(item, sessionSetting)

        when (type) {
            EditType.INSERT -> {

                processInsert(item)
            }
            EditType.EDIT -> {
                processUpdate(item)
            }
            else -> throw SessionException("EditType is not valid $type")
        }

        sentRefreshAllListener(type)

        return item
    }
    override fun scanInfo(info: Map<String, String>) {

        processShowError {
            if(!isRusLanguage()) throw Exception(ERROR_LANG_IS_NOT_RUS)

            scannerInfo.putAll(info)

            infoToCashPay()

            sentRefreshAllListener(EditType.EDIT)
        }
    }

    fun newPay() {
        scannerInfo.clear()
        createNewEntity()
        sentRefreshAllListener(EditType.EDIT)
    }

    fun savePay() {
        val entity = selectedEntity() ?: throw Exception("Не найден платеж для сохранения")

        entity.cashAccountId = AfinaQuery.getUserDepartment().accountId

        val session = AfinaQuery.uniqueSession()

        try {
            this.save(entity, session)

            val afinaId = AfinaQuery.execute(EXEC_SAVE_CASH_PAY, arrayOf<Any?>(entity.id), session,
                intArrayOf(OracleTypes.NUMBER))?.get(0) ?: throw Exception("afinaId is not found for call $EXEC_SAVE_CASH_PAY")

            entity.idAfinaDoc = (afinaId as Number).toLong()

            AfinaQuery.commitFree(session)
        } catch (e: java.lang.Exception) {
            AfinaQuery.rollbackFree(session)

            logger.error("savePay", e)

            throw java.lang.Exception(e.message)
        }
        reselectRow()

        selectedEntity()?.payerId?.let {
            if(!ClientPhysicService.isExistsClientById(it)) {
                ClientPhysicService.addClient(it, selectedEntity()!!.payerFio)
            }
        }
    }

    fun execPay() {

        val entity = selectedEntity() ?: throw Exception("Не найден платеж")

        if(entity.id != null && entity.idAfinaDoc != null) {
            save(entity)
        }

        val afinaId = entity.idAfinaDoc ?: throw Exception("Платеж не найден, либо еще не создан для исполнения")

        AfinaQuery.execute(EXECUTE_CASH_PAY, arrayOf<Any?>(afinaId) )

        reselectRow()
    }

    fun removePay() {
        val entity = selectedEntity() ?: throw Exception("Платеж не найден, либо еще не создан для удаления")

        if(entity.id == null && entity.idAfinaDoc == null) {
            dataList.remove(entity)

            val priorIndex = if(selectedRowIndex > 0 && selectedRowIndex >= dataList.size) dataList.size - 1
            else selectedRowIndex

            selectedRowIndex = priorIndex
        } else {
            AfinaQuery.execute(DELETE_CASH_PAY, arrayOf<Any?>(entity.id?:Long::class.javaObjectType,
                    entity.idAfinaDoc?:Long::class.javaObjectType) )

            reselectRow()
        }
    }

    fun print() {
        val entity = selectedEntity() ?: throw Exception("Платеж не найден, либо еще не создан")

        if(entity.idAfinaDoc == null) throw Exception("Док-т платежа физ. лиц еще не существует")

        val reportData = RtfPayCustomHouse(entity.idAfinaDoc!!) //if(entity.payeePactCode == "ТАМОЖ") RtfPayCustomHouse(entity.idAfinaDoc!!) else RtfPayKinderGarden(entity.idAfinaDoc!!)

        reportData.buildRtfReport()
    }

    fun infoUpdatedPayer() {

        logger.error("infoUpdatedPayer scannerTemp=$scannerTemp")

        if(scannerTemp == null) return

        val tempEntity = scannerTemp!!

        val entity = selectedEntity() ?: return

        if(tempEntity.payerInn.isNotBlank()) {
            entity.payerInn = tempEntity.payerInn
        }

        if(tempEntity.linePasport.isNotBlank()) {
            entity.linePasport = tempEntity.linePasport
        }

        if(tempEntity.numberPasport.isNotBlank()) {
            entity.numberPasport = tempEntity.numberPasport
        }

        if(tempEntity.dateIssued != null) {
            entity.dateIssued = tempEntity.dateIssued
        }

        if(tempEntity.typePasport != null) {
            entity.typePasport = tempEntity.typePasport
        }

        if(!tempEntity.pasportTypeName.isNullOrBlank() ) {
            entity.pasportTypeName = tempEntity.pasportTypeName
        }

        scannerTemp = null
    }

    private fun infoToCashPay(): CashPay {
        val entity = selectedEntity() ?: throw java.lang.Exception("Сначала нажмите кнопку <Новый платеж>") //createNewEntity()

        if(entity.idAfinaDoc != null && (entity.state in arrayOf(1L, 2L, -1L)) ) {
            throw Exception("Исполненые, удаленные и док-ты \"В кассе\" нельзя менять")
        }

        entity.amount = scannerInfo.findAmount() ?: entity.amount

        entity.payerFio = scannerInfo.findFio().ifEmpty(entity.payerFio)

        entity.payeeName = scannerInfo.findPayee().ifEmpty(entity.payeeName)

        entity.payerAddress = scannerInfo.findPayerAddres().ifEmpty(entity.payerAddress)

        entity.payeeInn = scannerInfo.findPayeeInn().ifEmpty(entity.payeeInn)

        entity.payeeKpp = scannerInfo.findPayeeKpp().ifEmpty(entity.payeeKpp)

        entity.payeeBik = scannerInfo.findPayeeBic().ifEmpty(entity.payeeBik)

        entity.payeeBankName = scannerInfo.findPayeeBankName().ifEmpty(entity.payeeBankName)

        entity.payeeAccount = scannerInfo.findPayeeAccount().ifEmpty(entity.payeeAccount)

        entity.detailAccount = scannerInfo.findDetailAccount().ifEmpty(entity.detailAccount)

        entity.detailPeriod = scannerInfo.findDetailPeriod().ifEmpty(entity.detailPeriod)

        entity.descriptionPay = scannerInfo.findDescription().ifEmpty(entity.descriptionPay)

        // custom fields
        entity.numberCashDoc = scannerInfo.docNumber() ?: entity.numberCashDoc

        entity.payerInn = scannerInfo.payerInn() ?: entity.payerInn

        entity.linePasport = scannerInfo.linePasport() ?: entity.linePasport

        entity.numberPasport = scannerInfo.numberPasport() ?: entity.numberPasport

        entity.dateIssued = scannerInfo.dateIssuedPassport() ?: entity.dateIssued

        entity.typePasport = scannerInfo.typePasport() ?: entity.typePasport

        entity.pasportTypeName = scannerInfo.pasportTypeName() ?: entity.pasportTypeName

        entity.payeePactId = scannerInfo.payeePactId() ?: entity.payeePactId

        entity.payeePactName = scannerInfo.payeePactName() ?: entity.payeePactName

        entity.payeeBankId = scannerInfo.payeeBankId() ?: entity.payeeBankId

        entity.payeePactCode = scannerInfo.payeePactCode() ?: entity.payeePactCode

        scannerTemp = entity.copy()

        return entity
    }

    private fun createNewEntity(): CashPay {
        return CashPay().apply {

            this.payeePactId = 1238476740
            this.detailPeriod = "10702000"

            dataList.add(this)
            setSelectedEntity(this)
        }
    }

    private const val DELETE_CASH_PAY = "{ call od.PTKB_CASH.deleteCashPay(?, ?) }"

    private const val EXEC_SAVE_CASH_PAY = "{ call od.PTKB_CASH.processCashPay(?, ?) }"

    private const val EXECUTE_CASH_PAY = "{ call od.PTKB_CASH.executeCashPay(?) }"

    private const val ERROR_LANG_IS_NOT_RUS = "при сканировании нужно включить русский язык :("
}

fun isRusLanguage() = InputContext.getInstance().locale.toString().contains("RU", true)

fun String.ifEmpty(other: String) = if(this.isEmpty()) other else this


