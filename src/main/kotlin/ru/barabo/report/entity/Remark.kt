package ru.barabo.report.entity

import ru.barabo.afina.SEQ_CLASSIFIED
import ru.barabo.db.annotation.*
import java.sql.Timestamp
import java.util.*

@SelectQuery("""
select r.ID, r.REPORT, r.STATE, r.REMARKER, r.CREATED, r.IS_HIDE, r.REMARK, 
      coalesce(u.pseudoname, r.REMARKER) NAME_REMARKER
from od.XLS_REPORT_REMARK r
left join od.users u on u.userid = r.REMARKER
where r.REPORT = ?
order by r.CREATED
""")
@TableName("OD.XLS_REPORT_REMARK")
data class Remark (
    @ColumnName("ID")
    @SequenceName(SEQ_CLASSIFIED)
    @ColumnType(java.sql.Types.BIGINT)
    var id: Long? = null,

    @ColumnName("REPORT")
    @ColumnType(java.sql.Types.BIGINT)
    var report: Long? = null,

    @ColumnName("STATE")
    var state: Long = 0,

    @ColumnName("REMARKER")
    var remarker: String = "",

    @ColumnName("CREATED")
    var created: Timestamp = Timestamp(Date().time),

    @ColumnName("IS_HIDE")
    var isHide: Long = 0,

    @ColumnName("REMARK")
    var remark: String = "",

    @ColumnName("NAME_REMARKER")
    @ReadOnly
    var remarkerName: String = ""
) {

    fun remarkInfo(): String = "$remarkerName ${isViewOnlyMe()} ${dateFormat.format(created)}\n $remark"

    private fun isViewOnlyMe(): String = if(isHide == 0L) "" else " (Видна только мне) "
}

private val dateFormat = java.text.SimpleDateFormat("dd.MM.yy")