package ru.barabo.report.entity

import ru.barabo.afina.SEQ_CLASSIFIED
import ru.barabo.db.annotation.*

@SelectQuery("{ ? = call OD.XLS_REPORT_ALL.getAccesGroupList(?) }")
@TableName("OD.XLS_REPORT_ACCESS")
data class AccessReport (
    @ColumnName("ID")
    @SequenceName(SEQ_CLASSIFIED)
    @ColumnType(java.sql.Types.BIGINT)
    var id: Long? = null,

    @ColumnName("REPORT")
    @ColumnType(java.sql.Types.BIGINT)
    var report: Long? = null,

    @ColumnName("ACCESS_GROUP")
    @ColumnType(java.sql.Types.BIGINT)
    var accessGroup: Long? = null,

    @ColumnName("CLASSIFIED")
    @ReadOnly
    var accessGroupClassified: Long? = null,

    @ColumnName("LABEL")
    @ReadOnly
    var groupName: String = "",

    @ColumnName("GROUPTYPE")
    @ReadOnly
    var groupType: Long = 0L,

    @ColumnName("GROUP_LABEL")
    @ReadOnly
    var groupTypeName: String = "",

    @ColumnName("IS_ACCESS")
    @ReadOnly
    var accessed: Long = 0L
) {
    var isAccess: Boolean
    get() = accessed != 0L
    set(value) {
        accessed =  if(value) 1L else 0L

        accessGroup = if(value) accessGroupClassified else null
    }
}
