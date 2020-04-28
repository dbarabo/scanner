package ru.barabo.scanner.entity

import ru.barabo.db.annotation.ColumnName
import ru.barabo.db.annotation.SelectQuery

@SelectQuery("{ ? = call OD.PTKB_CASH.getPassportTypes }")
data class PasportType(
        @ColumnName("CLASSIFIED")
        var id: Long? = null,

        @ColumnName("LABEL")
        var label: String = "",

        @ColumnName("ISRESIDENT")
        var isResident: Long? = null
) {
    override fun toString(): String = label
}