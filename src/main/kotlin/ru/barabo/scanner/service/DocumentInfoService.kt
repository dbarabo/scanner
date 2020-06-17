package ru.barabo.scanner.service

import org.apache.log4j.Logger
import ru.barabo.afina.AfinaOrm
import ru.barabo.db.annotation.ParamsSelect
import ru.barabo.db.service.StoreFilterService
import ru.barabo.scanner.entity.CashPay
import ru.barabo.scanner.entity.DocumentInfo

private val logger = Logger.getLogger(DocumentInfoService::class.simpleName)!!

object DocumentInfoService : StoreFilterService<DocumentInfo>(AfinaOrm, DocumentInfo::class.java), ParamsSelect {

    private var clientId: Long? = null

    override fun selectParams(): Array<Any?>? = arrayOf<Any?>(clientId)

    fun reselectDocumentInfo(idClient: Long) {
        clientId = idClient

        initData()
    }

    fun setDocInfoToCashPay(cashPay: CashPay) {

        val entity = getEntity(0) ?: return

        cashPay.typePasport = entity.jurdicalId
        cashPay.linePasport = entity.line
        cashPay.numberPasport = entity.number
        cashPay.dateIssued = entity.out
        cashPay.byIssued = entity.whoOut

        entity.jurdicalId?.let { PasportTypeService.setSelectEntityById(it) }
    }
}