package ru.barabo.scanner.service

import org.slf4j.LoggerFactory
import ru.barabo.afina.AfinaOrm
import ru.barabo.db.EditType
import ru.barabo.db.service.StoreFilterService
import ru.barabo.scanner.entity.CashPay
import ru.barabo.scanner.entity.ClientPhysic
import java.util.*

private val logger = LoggerFactory.getLogger(ClientPhysicService::class.java)!!

object ClientPhysicService : StoreFilterService<ClientPhysic>(AfinaOrm, ClientPhysic::class.java) {


    fun updatePayDocument(cashPay: CashPay) {

        val entity = selectedEntity() ?: return

        cashPay.payerFio = entity.label

        cashPay.payerId = entity.id

        logger.error("updatePayDocument cashPay.payerId=${cashPay.payerId}")
        logger.error("updatePayDocument cashPay.payerFio=${cashPay.payerFio}")

        logger.error("updatePayDocument entity.id=${entity.id}")

        entity.id?.let { DocumentInfoService.reselectDocumentInfo(it) }

        DocumentInfoService.setDocInfoToCashPay(cashPay)
    }

    fun isExistsClearDataIfNotEquals(cashPay: CashPay, fio: String): Boolean {
        val clientSelected = selectedEntity() ?: return false

        if(clientSelected.label.replace(" ", "").uppercase(Locale.getDefault()) !=
            fio.replace(" ", "").uppercase(Locale.getDefault()) ) {

             return DocumentInfoService.isExistsClearEqualsInfo(cashPay)
        }

        return false
    }

    fun isExistsClientById(idClient: Long): Boolean {
        return dataList.firstOrNull { it.id == idClient } != null
    }
    fun getIndexListById(idClient: Long): Int? = dataList.withIndex().firstOrNull {idClient == it.value.id }?.index

    fun addClient(id: Long, fio: String) {

        val client = ClientPhysic(fio, id)

        dataList.add(client)

        dataList.sortBy { it.label }

        setSelectedEntity(client)

        sentRefreshAllListener(EditType.INIT)
    }

    fun reselectAllData(cashPaySelected: CashPay?) {

        val selectedClient =  cashPaySelected?.payerId

        initData()

        selectedClient?.let { getIndexListById(it) }?.let {
            selectedRowIndex = it
        }

        sentRefreshAllListener(EditType.INIT)
    }

}