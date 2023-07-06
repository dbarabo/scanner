package ru.barabo.scanner.service

import ru.barabo.afina.AfinaOrm
import ru.barabo.db.annotation.ParamsSelect
import ru.barabo.db.service.StoreFilterService
import ru.barabo.scanner.entity.CashPay
import ru.barabo.scanner.entity.DocumentInfo

//private val logger = LoggerFactory.getLogger(DocumentInfoService::class.java)!!

object DocumentInfoService : StoreFilterService<DocumentInfo>(AfinaOrm, DocumentInfo::class.java), ParamsSelect {

    private var clientId: Long? = null

    override fun selectParams(): Array<Any?>? = arrayOf(clientId)

    fun reselectDocumentInfo(idClient: Long) {
        clientId = idClient

        initData()
    }

    fun isExistsClearEqualsInfo(cashPay: CashPay): Boolean {
        val docInfo = getEntity(0) ?: return false

        var isExists = false

        if(docInfo.line == cashPay.linePasport) {
            cashPay.linePasport = ""
            isExists = true
        }

        if(docInfo.number == cashPay.numberPasport) {
            cashPay.numberPasport = ""
            isExists = true
        }

        if(docInfo.out == cashPay.dateIssued && cashPay.dateIssued != null) {
            cashPay.dateIssued = null
            isExists = true
        }

        if(docInfo.whoOut == cashPay.byIssued) {
            cashPay.byIssued = ""
            isExists = true
        }

        if(docInfo.codeOut == cashPay.departmentCode) {
            cashPay.departmentCode = ""
            isExists = true
        }

        if(docInfo.codeOut == cashPay.departmentCode) {
            cashPay.departmentCode = ""
            isExists = true
        }

        if(docInfo.inn == cashPay.payerInn) {
            cashPay.payerInn = ""
            isExists = true
        }

        if(docInfo.birthday == cashPay.birthDate && cashPay.birthDate != null) {
            cashPay.birthDate = null
            isExists = true
        }

        if(docInfo.birthPlace == cashPay.birthPlace) {
            cashPay.birthPlace = ""
            isExists = true
        }

        if(docInfo.address == cashPay.payerAddress) {
            cashPay.payerAddress = ""
            isExists = true
        }

        cashPay.payerId = null

        return isExists
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