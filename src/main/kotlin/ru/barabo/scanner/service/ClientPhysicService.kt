package ru.barabo.scanner.service

import org.apache.log4j.Logger
import ru.barabo.afina.AfinaOrm
import ru.barabo.db.service.StoreFilterService
import ru.barabo.scanner.entity.CashPay
import ru.barabo.scanner.entity.ClientPhysic

private val logger = Logger.getLogger(ClientPhysicService::class.simpleName)!!

object ClientPhysicService : StoreFilterService<ClientPhysic>(AfinaOrm, ClientPhysic::class.java) {


    fun updatePayDocuments(cashPay: CashPay) {

        val entity = selectedEntity() ?: return

        cashPay.payerFio = entity.label

        cashPay.payerId = entity.id

        entity.id?.let { DocumentInfoService.reselectDocumentInfo(it) }

        DocumentInfoService.setDocInfoToCashPay(cashPay)
    }
}