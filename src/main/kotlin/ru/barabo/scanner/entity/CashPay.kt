package ru.barabo.scanner.entity

import ru.barabo.afina.SEQ_CLASSIFIED
import ru.barabo.db.annotation.ColumnName
import ru.barabo.db.annotation.ColumnType
import ru.barabo.db.annotation.SelectQuery
import ru.barabo.db.annotation.SequenceName
import java.sql.Timestamp

@SelectQuery("select * from dual where 1 = 0")
data class CashPay(
        @ColumnName("ID")
        @SequenceName(SEQ_CLASSIFIED)
        @ColumnType(java.sql.Types.BIGINT)
        var id: Long? = null,

        @ColumnName("STATE")
        var state: Long = 0L,

        @ColumnName("LABEL")
        var numberCashDoc: String = "",

        @ColumnName("CASH_ACCOUNT")
        var cashAccount: String = "",

        @ColumnName("PAY_ACCOUNT")
        var payAccount: String = "",

        @ColumnName("AMOUNT")
        var amount: Double = 0.0,

        @ColumnName("CASH_SYMBOL1")
        var cashSymbol1: String = "",

        @ColumnName("CASH_SYMBOL2")
        var cashSymbol2: String = "",

        @ColumnName("DESCRIPTION_PAY")
        var descriptionPay: String = "",

        @ColumnName("PAYER_FIO")
        var payerFio: String = "",

        @ColumnName("PAYER_ID")
        var payerId: Long? = null,

        @ColumnName("IS_RESIDENT")
        var isResident: Long = 1L,

        @ColumnName("REGION")
        var region: String = "",

        @ColumnName("BIRTH_PLACE")
        var birthPlace: String = "",

        @ColumnName("BIRTH_DAY")
        var birthDate: Timestamp? = null,

        @ColumnName("TYPE_PASPORT")
        var typePasport: Long = 0,

        @ColumnName("LINE_PASPORT")
        var linePasport: String = "",

        @ColumnName("NUMBER_PASPORT")
        var numberPasport: String = "",

        @ColumnName("DATE_ISSUED")
        var dateIssued: Timestamp? = null,

        @ColumnName("BY_ISSUED")
        var byIssued: String = "",

        @ColumnName("CODE_DEPARTMENT")
        var departmentCode: String = "",

        @ColumnName("PAYEE_PACT")
        var payeePactId: Long = 0,

        @ColumnName("PAYEE_ID")
        var payeeId: Long? = null,

        @ColumnName("PAYEE_NAME")
        var payeeName: String = "",

        @ColumnName("PAYEE_INN")
        var payeeInn: String = "",

        @ColumnName("PAYEE_KPP")
        var payeeKpp: String = "",

        @ColumnName("PAYEE_BANK_BIK")
        var payeeBik: String = "",

        @ColumnName("PAYEE_BANK_NAME")
        var payeeBankName: String = "",

        @ColumnName("PAYEE_ACCOUNT")
        var payeeAccount: String = "",

        @ColumnName("DETAIL_ACCOUNT")
        var detailAccount: String = "",

        @ColumnName("DETAIL_PERIOD")
        var detailPeriod: String = "",

        @ColumnName("DETAIL_TARGET")
        var detailTarget: String = "",

        @ColumnName("COMMISION_AMOUNT")
        var commisionAmount: Double = 0.0,

        @ColumnName("COMMISION_ACCOUNT")
        var commisionAccount: String = "",

        @ColumnName("COMMISION_TYPE")
        var commisionType: Long = 0
) {
        override fun toString(): String = "amount=$amount payerFio=$payerFio payeeName=$payeeName payeeInn=$payeeInn " +
                "payeeKpp=$payeeKpp payeeBik=$payeeBik payeeAccount=$payeeAccount detailAccount=$detailAccount detailPeriod=$detailPeriod descriptionPay=$descriptionPay"
}