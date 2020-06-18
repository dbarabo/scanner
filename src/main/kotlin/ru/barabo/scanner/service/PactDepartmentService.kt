package ru.barabo.scanner.service

import ru.barabo.afina.AfinaOrm
import ru.barabo.db.service.StoreFilterService
import ru.barabo.scanner.entity.CashPay
import ru.barabo.scanner.entity.PactDepartment

object PactDepartmentService :  StoreFilterService<PactDepartment>(AfinaOrm, PactDepartment::class.java) {

    fun setSelectEntityById(id: Long) {

        val withIndex = dataList.withIndex().firstOrNull { it.value.id == id } ?: return

        selectedRowIndex = withIndex.index
    }

    fun updatePayDocument(oldPact: PactDepartment?, cashPay: CashPay) {

        val pact = selectedEntity() ?: return

        cashPay.payeePactId = pact.id
        cashPay.payeePactName = pact.label

        if(oldPact?.id == pact.id) return

        cashPay.payAccount = pact.payAccount ?: ""

        cashPay.payeeAccount = pact.setCheckPack(oldPact, cashPay.payeeAccount, pact.payeeAccount)

        cashPay.payeeBankId = pact.setCheckPack(oldPact, cashPay.payeeBankId, pact.payeeBankId)

        cashPay.payeeBik = pact.setCheckPack(oldPact, cashPay.payeeBik, pact.payeeBik)

        cashPay.payeeBankName = pact.setCheckPack(oldPact, cashPay.payeeBankName, pact.payeeBankName)

        cashPay.payeeName = pact.setCheckPack(oldPact, cashPay.payeeName, pact.payeeName)

        cashPay.payeeInn = pact.setCheckPack(oldPact, cashPay.payeeInn, pact.payeeInn)

        cashPay.payeeKpp = pact.setCheckPack(oldPact, cashPay.payeeKpp, pact.payeeKpp)

        cashPay.descriptionPay = pact.setCheckPack(oldPact, cashPay.descriptionPay, pact.description)
    }
}

private fun PactDepartment.setCheckPack(oldPact: PactDepartment?, oldValue: String, newValue: String?): String {

    return if((oldPact?.isPayWithOutPact == false) || (!isPayWithOutPact) ) newValue ?: "" else oldValue
}

private fun PactDepartment.setCheckPack(oldPact: PactDepartment?, oldValue: Long?, newValue: Long?): Long? {

    return if((oldPact?.isPayWithOutPact == false) || (!isPayWithOutPact) ) newValue else oldValue
}