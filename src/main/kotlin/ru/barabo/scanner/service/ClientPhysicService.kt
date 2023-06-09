package ru.barabo.scanner.service

import org.apache.log4j.Logger
import org.slf4j.LoggerFactory
import ru.barabo.afina.AfinaOrm
import ru.barabo.db.service.StoreFilterService
import ru.barabo.scanner.entity.CashPay
import ru.barabo.scanner.entity.ClientPhysic
import ru.barabo.scanner.main.Scanner

private val logger = LoggerFactory.getLogger(ClientPhysicService::class.java)!!

object ClientPhysicService : StoreFilterService<ClientPhysic>(AfinaOrm, ClientPhysic::class.java) {


    fun updatePayDocument(cashPay: CashPay) {

        val entity = selectedEntity() ?: return

        cashPay.payerFio = entity.label

        cashPay.payerId = entity.id

        entity.id?.let { DocumentInfoService.reselectDocumentInfo(it) }

        DocumentInfoService.setDocInfoToCashPay(cashPay)
    }
}