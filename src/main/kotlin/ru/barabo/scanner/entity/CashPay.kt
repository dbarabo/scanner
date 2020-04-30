package ru.barabo.scanner.entity

import ru.barabo.afina.SEQ_CLASSIFIED
import ru.barabo.db.annotation.*
import java.sql.Timestamp

@SelectQuery("select * from dual where 1 = 0")
@TableName("OD.PTKB_CASH_PAY")
data class CashPay(
        @ColumnName("ID")
        @SequenceName(SEQ_CLASSIFIED)
        @ColumnType(java.sql.Types.BIGINT)
        var id: Long? = null,

        @ColumnName("AFINA_DOC")
        @ColumnType(java.sql.Types.BIGINT)
        var idAfinaDoc: Long? = null,

        @ColumnName("STATE")
        @ColumnType(java.sql.Types.BIGINT)
        var state: Long = 0L,

        @ColumnName("DOC_NUMBER")
        var numberCashDoc: String = "",

        @ColumnName("CASH_ACCOUNT_CODE")
        @ReadOnly
        var cashAccount: String = "",

        @ColumnName("CASH_ACCOUNT")
        var cashAccountId: Long? = null,

        @ColumnName("PAY_ACCOUNT")
        @ReadOnly
        var payAccount: String = "",

        @ColumnName("AMOUNT")
        var amount: Double = 0.0,

        @ColumnName("CASH_SYMBOL1")
        @ReadOnly
        var cashSymbol1: String = "",

        @ColumnName("CASH_SYMBOL2")
        @ReadOnly
        var cashSymbol2: String = "",

        @ColumnName("DESCRIPTION_PAY")
        var descriptionPay: String = "",

        @ColumnName("PAYER_FIO")
        var payerFio: String = "",

        @ColumnName("PAYER_CLIENT")
        @ColumnType(java.sql.Types.BIGINT)
        var payerId: Long? = null,

        @ColumnName("IS_RESIDENT")
        var isResident: Long = 1L,

        @ColumnName("PAYER_REGION_ID")
        @ColumnType(java.sql.Types.BIGINT)
        var regionId: Long? = null,

        @ColumnName("REGION")
        @ReadOnly
        var region: String = "",

        @ColumnName("PAYER_BIRTH_PLACE")
        var birthPlace: String = "",

        @ColumnName("PAYER_BIRTH_DATE")
        @ColumnType(java.sql.Types.TIMESTAMP)
        var birthDate: Timestamp? = null,

        @ColumnName("PAYER_DOC_TYPE")
        @ColumnType(java.sql.Types.BIGINT)
        var typePasport: Long? = null,

        @ColumnName("PAYER_DOC_LINE")
        var linePasport: String = "",

        @ColumnName("PAYER_DOC_NUMBER")
        var numberPasport: String = "",

        @ColumnName("PAYER_DOC_ISSUED")
        @ColumnType(java.sql.Types.TIMESTAMP)
        var dateIssued: Timestamp? = null,

        @ColumnName("PAYER_DOC_BY_ISSUED")
        var byIssued: String = "",

        @ColumnName("PAYER_DOC_CODE_DEPARTMENT")
        var departmentCode: String = "",

        @ColumnName("PHYS_PACT_ID")
        @ReadOnly
        var payeePactId: Long? = null,

        @ColumnName("PAYEE_ID")
        @ColumnType(java.sql.Types.BIGINT)
        var payeeId: Long? = null,

        @ColumnName("PAYEE_NAME")
        var payeeName: String = "",

        @ColumnName("PAYEE_INN")
        var payeeInn: String = "",

        @ColumnName("PAYEE_KPP")
        var payeeKpp: String = "",

        @ColumnName("PAYEE_BIK")
        var payeeBik: String = "",

        @ColumnName("PAYEE_BANK_ID")
        @ColumnType(java.sql.Types.BIGINT)
        var payeeBankId: Long? = null,

        @ColumnName("PAYEE_BANK_NAME")
        @ReadOnly
        var payeeBankName: String = "",

        @ColumnName("PAYEE_ACCOUNT_CODE")
        var payeeAccount: String = "",

        @ColumnName("PERSONAL_ACCOUNT_PAY")
        var detailAccount: String = "",

        @ColumnName("PERIOD_PAY")
        var detailPeriod: String = "",

        @ColumnName("TARGET_PAY")
        @ColumnType(java.sql.Types.BIGINT)
        var detailTargetId: Long? = null,

        @ColumnName("TARGET_PAY_INFO")
        @ReadOnly
        var detailTarget: String = "",

        @ColumnName("COMMIS_AMOUNT")
        var commisionAmount: Double = 0.0,

        @ColumnName("COMMIS_ACCOUNT")
        var commisionAccount: String = "",

        @ColumnName("COMMIS_TYPE")
        @ColumnType(java.sql.Types.BIGINT)
        var commisionType: Long? = null
) {
        var stateName: String
        get() = when(state) {
                0L -> "Новый"
                else -> "хз"
        }
        set(_) {}


        override fun toString(): String = "amount=$amount payerFio=$payerFio payeeName=$payeeName payeeInn=$payeeInn " +
                "payeeKpp=$payeeKpp payeeBik=$payeeBik payeeAccount=$payeeAccount detailAccount=$detailAccount detailPeriod=$detailPeriod descriptionPay=$descriptionPay"
}