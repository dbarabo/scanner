package ru.barabo.scanner.entity

import ru.barabo.db.annotation.ColumnName
import ru.barabo.db.annotation.ColumnType
import ru.barabo.db.annotation.SelectQuery
import java.sql.Timestamp

@SelectQuery("{ ? = call OD.PTKB_CASH.getPassportClient( ? ) }")
data class DocumentInfo (
        @ColumnName("CLIENT_ID")
        var clientId: Long? = null,

        @ColumnName("LINE")
        var line: String = "",

        @ColumnName("NUMBER")
        var number: String = "",

        @ColumnName("OUT_DATE")
        @ColumnType(java.sql.Types.TIMESTAMP)
        var out: Timestamp? = null,

        @ColumnName("WHO")
        var whoOut: String = "",

        @ColumnName("JURDICAL_ID")
        var jurdicalId: Long? = null
) {
        override fun toString(): String = jurdicalId?.toString() ?: "NULL jurdicalId clientId=$clientId"
}