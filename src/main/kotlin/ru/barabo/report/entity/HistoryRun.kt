package ru.barabo.report.entity

import ru.barabo.afina.SEQ_CLASSIFIED
import ru.barabo.db.annotation.*
import java.sql.Timestamp
import java.util.*

@SelectQuery("""select h.id, h.report, h.state, h.VERSION_ID, h.RUNNER, h.RUNNED, h.ERROR, h.FILE_NAME, h.WORK_PLACE,
coalesce(u.pseudoname, h.RUNNER) NAME_RUNNER, w.label WORKPLACE_NAME
from od.XLS_HISTORY_RUN h
left join od.users u on u.userid = h.RUNNER
left join od.WorkPlace w on w.classified = h.WORK_PLACE
where h.REPORT = ?
  and (h.RUNNER = user or 1000005945 = ?) 
order by h.RUNNED desc""")
@TableName("OD.XLS_HISTORY_RUN")
data class HistoryRun(
    @ColumnName("ID")
    @SequenceName(SEQ_CLASSIFIED)
    @ColumnType(java.sql.Types.BIGINT)
    var id: Long? = null,

    @ColumnName("REPORT")
    @ColumnType(java.sql.Types.BIGINT)
    var report: Long? = null,

    @ColumnName("STATE")
    var state: Long = 0,

    @ColumnName("VERSION_ID")
    var versionId: Long = 0,

    @ColumnName("RUNNER")
    var runner: String = "",

    @ColumnName("RUNNED")
    var runned: Timestamp = Timestamp(Date().time),

    @ColumnName("ERROR")
    var error: String = "",

    @ColumnName("FILE_NAME")
    var fileName: String = "",

    @ColumnName("WORK_PLACE")
    var workPlaceId: Long = 0L,

    @ColumnName("NAME_RUNNER")
    @ReadOnly
    var runnerName: String = "",

    @ColumnName("WORKPLACE_NAME")
    @ReadOnly
    var workPlaceName: String = ""
) {
    var info: String
    get() = "<html>${dateTimeFormat.format(runned)}<br>$runnerName<br>$workPlaceName<br>${fileName.substringAfterLast('/')}</html>"
    set(value) {}
}

private val dateTimeFormat = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm:ss")