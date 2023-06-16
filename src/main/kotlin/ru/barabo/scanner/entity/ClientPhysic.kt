package ru.barabo.scanner.entity

import ru.barabo.db.annotation.ColumnName
import ru.barabo.db.annotation.SelectQuery

@SelectQuery("{ ? = call OD.PTKB_CASH.getClientPhysicByUserDepart }")
data class ClientPhysic(
        @ColumnName("LABEL")
        var label: String = "",

        @ColumnName("CLASSIFIED")
        var id: Long? = null
) {
    override fun toString(): String = label
}