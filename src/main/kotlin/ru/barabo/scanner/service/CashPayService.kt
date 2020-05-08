package ru.barabo.scanner.service

import oracle.jdbc.OracleTypes
import org.apache.log4j.Logger
import ru.barabo.afina.AfinaOrm
import ru.barabo.afina.AfinaQuery
import ru.barabo.db.EditType
import ru.barabo.db.annotation.QuerySelect
import ru.barabo.db.service.StoreFilterService
import ru.barabo.gui.swing.processShowError
import ru.barabo.scanner.entity.CashPay
import java.awt.im.InputContext

object CashPayService : StoreFilterService<CashPay>(AfinaOrm, CashPay::class.java), QuerySelect, ScanEventListener {

    private val logger = Logger.getLogger(CashPayService::class.simpleName)!!

    private val scannerInfo: MutableMap<String, String> = LinkedHashMap()

    override fun selectQuery(): String = "{ ? = call od.PTKB_CASH.getCashPayList }"

    override fun processInsert(item: CashPay) {}

    override fun callBackSelectData(item: CashPay) {
        synchronized(dataList) { dataList.add(item) }
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

    private fun infoToCashPay(): CashPay {
        val entity = selectedEntity() ?: createNewEntity()

        entity.amount = scannerInfo.findAmount() ?: entity.amount

        entity.payerFio = scannerInfo.findFio().ifEmpty(entity.payerFio)

        entity.payeeName = scannerInfo.findPayee().ifEmpty(entity.payeeName)

        entity.payeeInn = scannerInfo.findPayeeInn().ifEmpty(entity.payeeInn)

        entity.payeeKpp = scannerInfo.findPayeeKpp().ifEmpty(entity.payeeKpp)

        entity.payeeBik = scannerInfo.findPayeeBic().ifEmpty(entity.payeeBik)

        entity.payeeBankName = scannerInfo.findPayeeBankName().ifEmpty(entity.payeeBankName)

        entity.payeeAccount = scannerInfo.findPayeeAccount().ifEmpty(entity.payeeAccount)

        entity.detailAccount = scannerInfo.findDetailAccount().ifEmpty(entity.detailAccount)

        entity.detailPeriod = scannerInfo.findDetailPeriod().ifEmpty(entity.detailPeriod)

        entity.descriptionPay = scannerInfo.findDescription().ifEmpty(entity.descriptionPay)

        logger.error(entity.toString())

        return entity
    }

    private fun createNewEntity(): CashPay {
        return CashPay().apply {
            dataList.add(this)
            setSelectedEntity(this)
        }
    }

    private const val EXEC_SAVE_CASH_PAY = "{ call od.PTKB_CASH.processCashPay(?, ?) }"

    private const val ERROR_LANG_IS_NOT_RUS = "при сканировании нужно включить русский язык :("
}

fun isRusLanguage() = InputContext.getInstance().locale.toString().contains("RU", true)

fun String.ifEmpty(other: String) = if(this.isEmpty()) other else this


