package ru.barabo.scanner.entity

import ru.barabo.afina.SEQ_CLASSIFIED
import ru.barabo.db.annotation.*
import java.lang.Exception
import java.sql.Timestamp
import java.text.DecimalFormat

@SelectQuery("""
select cp.*, od.PTKB_CASH.getClientLabel(cp.PAYEE_BANK_ID) PAYEE_BANK_NAME, od.accountCode(cp.cash_account) CASH_ACCOUNT_CODE,
   od.PTKB_CASH.getPasportTypeName(cp.PAYER_DOC_TYPE) PASSPORT_NAME, od.PTKB_CASH.getPactName(cp.PHYS_PACT_ID) PACT_NAME,
   od.PTKB_CASH.getPactCode(cp.PHYS_PACT_ID) PACT_CODE
from od.PTKB_CASH_PAY cp """)
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
        @ColumnType(java.sql.Types.VARCHAR)
        var numberCashDoc: String = "",

        @ColumnName("CASH_ACCOUNT_CODE")
        @ReadOnly
        var cashAccount: String = "",

        @ColumnName("CASH_ACCOUNT")
        @ColumnType(java.sql.Types.BIGINT)
        var cashAccountId: Long? = null,

        @ColumnName("PAY_ACCOUNT")
        @ReadOnly
        var payAccount: String = "",

        @ColumnName("PAYER_ADDRESS")
        var payerAddress: String = "",

        @ColumnName("AMOUNT")
        var amount: Double = 0.0,

        @ColumnName("CASH_SYMBOL1")
        @ReadOnly
        var cashSymbol1: String = "",

        @ColumnName("CASH_SYMBOL2")
        @ReadOnly
        var cashSymbol2: String = "",

        @ColumnName("DESCRIPTION_PAY")
        @ColumnType(java.sql.Types.VARCHAR)
        var descriptionPay: String = "",

        @ColumnName("PAYER_FIO")
        @ColumnType(java.sql.Types.VARCHAR)
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
        @ColumnType(java.sql.Types.VARCHAR)
        var birthPlace: String = "",

        @ColumnName("PAYER_BIRTH_DATE")
        @ColumnType(java.sql.Types.TIMESTAMP)
        var birthDate: Timestamp? = null,

        @ColumnName("PAYER_DOC_TYPE")
        @ColumnType(java.sql.Types.BIGINT)
        var typePasport: Long? = null,

        @ColumnName("PAYER_DOC_LINE")
        @ColumnType(java.sql.Types.VARCHAR)
        var linePasport: String = "",

        @ColumnName("PAYER_DOC_NUMBER")
        @ColumnType(java.sql.Types.VARCHAR)
        var numberPasport: String = "",

        @ColumnName("PASSPORT_NAME")
        @ReadOnly
        var pasportTypeName: String? = null,

        @ColumnName("PAYER_DOC_ISSUED")
        @ColumnType(java.sql.Types.TIMESTAMP)
        var dateIssued: Timestamp? = null,

        @ColumnName("PAYER_DOC_BY_ISSUED")
        @ColumnType(java.sql.Types.VARCHAR)
        var byIssued: String = "",

        @ColumnName("PAYER_DOC_CODE_DEPARTMENT")
        var departmentCode: String = "",

        @ColumnName("PHYS_PACT_ID")
        @ColumnType(java.sql.Types.BIGINT)
        var payeePactId: Long? = null,

        @ColumnName("PACT_NAME")
        @ReadOnly
        var payeePactName: String? = null,

        @ColumnName("PACT_CODE")
        @ReadOnly
        var payeePactCode: String? = null,

        @ColumnName("PAYEE_ID")
        @ColumnType(java.sql.Types.BIGINT)
        var payeeId: Long? = null,

        @ColumnName("PAYEE_NAME")
        @ColumnType(java.sql.Types.VARCHAR)
        var payeeName: String = "",

        @ColumnName("PAYEE_INN")
        @ColumnType(java.sql.Types.VARCHAR)
        var payeeInn: String = "",

        @ColumnName("PAYEE_KPP")
        @ColumnType(java.sql.Types.VARCHAR)
        var payeeKpp: String = "",

        @ColumnName("PAYEE_BIK")
        @ColumnType(java.sql.Types.VARCHAR)
        var payeeBik: String = "",

        @ColumnName("PAYEE_BANK_ID")
        @ColumnType(java.sql.Types.BIGINT)
        var payeeBankId: Long? = null,

        @ColumnName("PAYEE_BANK_NAME")
        @ReadOnly
        var payeeBankName: String = "",

        @ColumnName("PAYEE_ACCOUNT_CODE")
        @ColumnType(java.sql.Types.VARCHAR)
        var payeeAccount: String = "",

        @ColumnName("PERSONAL_ACCOUNT_PAY")
        @ColumnType(java.sql.Types.VARCHAR)
        var detailAccount: String = "",

        @ColumnName("PERIOD_PAY")
        @ColumnType(java.sql.Types.VARCHAR)
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
        @ColumnType(java.sql.Types.VARCHAR)
        var commisionAccount: String = "",

        @ColumnName("COMMIS_TYPE")
        @ColumnType(java.sql.Types.BIGINT)
        var commisionType: Long? = null,

        @ColumnName("PAYER_INN")
        @ColumnType(java.sql.Types.VARCHAR)
        var payerInn: String = ""
) {
        var stateName: String
        get() = when(state) {
                0L -> id?.let { "Создан" } ?: "Новый"
                1L -> "Исполнен"
                2L -> "Удален"
                -1L -> "В кассе"
                else -> "хз"
        }
        set(_) {}

        var amountFormat: String
        get() = amount.formatedCurrency()
        set(_) {}


        fun checkFieldsBeforeSave() {
                if(cashAccountId == null) throw Exception("Не указан номер счета кабинки кассы")

                if(payeePactCode?.uppercase() != "ТАМОЖ") return

                if(payerAddress.isBlank()) throw Exception("Заполните адрес")

                if(amount<= 0.0) throw Exception("Сумма не может быть нулевой")

                if(payerFio.isBlank()) throw Exception("Заполните ФИО Плательщика")

                if(birthPlace.isBlank()) throw Exception("Заполните Место рождения")

                if(birthDate == null) throw Exception("Заполните Дату рождения")

                if(linePasport.length != 4) throw Exception("Серия паспорта должна состоять из 4-х цифр")

                if(numberPasport.length != 6) throw Exception("Номер паспорта должен состоять из 6-х цифр")

                if(dateIssued == null) throw Exception("Заполните Дату выдачи паспорта")

                if(byIssued.isBlank()) throw Exception("Заполните поле 'Кем выдан паспорт'")

                if(departmentCode.isBlank()) throw Exception("Заполните поле 'Код подразделения'")

                if(detailPeriod.length != 8) throw Exception("Заполните поле Код таможни в поле 'Период оплаты' он должен состоять из 8-ми цифр")

                if(payerInn.length != 12) throw Exception("ИНН плательщика должен состоять из 12-ти цифр")
        }

        override fun toString(): String = """
amount=$amount payerFio=$payerFio payeeName=$payeeName payeeInn=$payeeInn 
payeeKpp=$payeeKpp payeeBik=$payeeBik payeeAccount=$payeeAccount detailAccount=$detailAccount 
detailPeriod=$detailPeriod descriptionPay=$descriptionPay payerInn=$payerInn              
""".trimIndent()
}

private fun Number?.formatedCurrency() = this?.let { DecimalFormat("0.00").format(it) } ?: ""