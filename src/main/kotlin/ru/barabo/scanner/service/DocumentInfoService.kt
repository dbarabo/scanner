package ru.barabo.scanner.service

import org.apache.log4j.Logger
import org.slf4j.LoggerFactory
import ru.barabo.afina.AfinaOrm
import ru.barabo.db.annotation.ColumnName
import ru.barabo.db.annotation.ParamsSelect
import ru.barabo.db.service.StoreFilterService
import ru.barabo.scanner.entity.CashPay
import ru.barabo.scanner.entity.DocumentInfo
import java.sql.Timestamp

private val logger = LoggerFactory.getLogger(DocumentInfoService::class.java)!!

object DocumentInfoService : StoreFilterService<DocumentInfo>(AfinaOrm, DocumentInfo::class.java), ParamsSelect {

    private var clientId: Long? = null

    override fun selectParams(): Array<Any?>? = arrayOf(clientId)

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
        cashPay.departmentCode = entity.codeOut

        cashPay.payerInn = entity.inn
        cashPay.birthDate = entity.birthday
        cashPay.birthPlace = entity.birthPlace
        cashPay.payerAddress = entity.address

        cashPay.typePasport?.let { PasportTypeService.setSelectEntityById(it) }
    }
}