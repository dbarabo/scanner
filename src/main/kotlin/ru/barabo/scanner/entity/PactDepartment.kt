package ru.barabo.scanner.entity

import ru.barabo.db.annotation.ColumnName
import ru.barabo.db.annotation.SelectQuery

@SelectQuery("{ ? = call OD.PTKB_CASH.getPactByUserDepartment }")
data class PactDepartment(
        @ColumnName("CLASSIFIED")
        var id: Long? = null,

        @ColumnName("LABEL")
        var label: String? = null,

        @ColumnName("IS_NO_PACT")
        var isNoPact: Long? = null,

        @ColumnName("PAY_ACCOUNT_ID")
        var payAccountId: Long? = null,

        @ColumnName("PAY_ACCOUNT")
        var payAccount: String? = null,

        @ColumnName("PAYEE_ACCOUNT_CODE")
        var payeeAccount: String? = null,

        @ColumnName("PAYEE_BANK_ID")
        var payeeBankId: Long? = null,

        @ColumnName("PAYEE_BIK")
        var payeeBik: String? = null,

        @ColumnName("PAYEE_BANK_NAME")
        var payeeBankName: String? = null,

        @ColumnName("PAYEE_NAME")
        var payeeName: String? = null,

        @ColumnName("PAYEE_INN")
        var payeeInn: String? = null,

        @ColumnName("PAYEE_KPP")
        var payeeKpp: String? = null,

        @ColumnName("DESCRIPTION")
        var description: String? = null
) {
    val isPayWithOutPact: Boolean  get() = (isNoPact != 0L)

    override fun toString(): String = label ?: ""
}
