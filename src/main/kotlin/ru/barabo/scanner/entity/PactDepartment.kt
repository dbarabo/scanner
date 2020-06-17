package ru.barabo.scanner.entity

import ru.barabo.db.annotation.ColumnName
import ru.barabo.db.annotation.SelectQuery

@SelectQuery("{ ? = call OD.PTKB_CASH.getPactByUserDepartment }")
data class PactDepartment(
        @ColumnName("CLASSIFIED")
        var id: Long? = null,

        @ColumnName("LABEL")
        var label: String = ""
) {
    override fun toString(): String = label
}