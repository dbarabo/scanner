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
import ru.barabo.gui.swing.processShowError
import ru.barabo.scanner.entity.CashPay
import java.awt.im.InputContext

object CashPayService : StoreFilterService<CashPay>(AfinaOrm, CashPay::class.java), QuerySelect, ScanEventListener {

    private val logger = LoggerFactory.getLogger(CashPayService::class.java)!!

    private val scannerInfo: MutableMap<String, String> = LinkedHashMap()

    private var scannerTemp: CashPay? = null

    override fun selectQuery(): String = "{ ? = call od.PTKB_CASH.getCashPayList }"

    override fun processInsert(item: CashPay) {}

    override fun save(item: CashPay, sessionSetting: SessionSetting): CashPay {

        item.checkFieldsBeforeSave()

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

            for ((key, value) in info) {
                scannerInfo.putIfAbsent(key, value)
            }

            logger.error("scanInfo")
            info.forEach { (t, u) -> logger.error("$t=$u") }

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

        save(entity)

        val afinaId = AfinaQuery.execute(EXEC_SAVE_CASH_PAY, arrayOf<Any?>(entity.id),
                intArrayOf(OracleTypes.NUMBER))?.get(0) ?: throw Exception("afinaId is not found for call $EXEC_SAVE_CASH_PAY")

        entity.idAfinaDoc = (afinaId as Number).toLong()

        reselectRow()
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

        val reportData = if(entity.payeePactCode == "ТАМОЖ") RtfPayCustomHouse(entity.idAfinaDoc!!) else RtfPayKinderGarden(entity.idAfinaDoc!!)

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

        if(entity.idAfinaDoc != null && (entity.state == 1L || entity.state == 2L)) {
            throw Exception("Исполненые либо удаленные док-ты нельзя менять")
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

        logger.error("infoToCashPay entity=$entity")
        logger.error("infoToCashPay scannerTemp=$scannerTemp")

        return entity
    }

    private fun createNewEntity(): CashPay {
        return CashPay().apply {
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


