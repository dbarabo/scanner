package ru.barabo.scanner.entity

import ru.barabo.db.annotation.ColumnName
import ru.barabo.db.annotation.SelectQuery

@SelectQuery("{ ? = call OD.XLS_REPORT_ALL.getClientPhysic }")
data class ClientPhysic(
        @ColumnName("LABEL")
        var label: String = "",

        @ColumnName("ID_CLIENT")
        var id: Long? = null
) {
    override fun toString(): String = label
}