package ru.barabo.report.entity

import ru.barabo.db.annotation.*

@SelectQuery("""
select d.* 
from od.xls_directory d
where 
(1000005945 = ? or 
  exists (select * 
     from od.xls_report r
     where r.directory in (select d2.id 
         from od.xls_directory d2
         where d.id in (d2.parent, d2.id)  )
      and r.state = 1)
)
order by case when d.parent is null then 1000000*d.id else 1000000*d.parent + d.id end
""")
    //"select d.* from od.xls_directory d order by case when d.parent is null then 1000000*d.id else 1000000*d.parent + d.id end")
@TableName("OD.XLS_DIRECTORY")
data class Directory(
    @ColumnName("ID")
    @SequenceName(SEQ_XLS_DIRECTORY)
    @ColumnType(java.sql.Types.BIGINT)
    var id: Long? = null,

    @ColumnName("PARENT")
    @ColumnType(java.sql.Types.BIGINT)
    var parent: Long? = null,

    @ColumnName("STATE")
    var state: Long = 0,

    @ColumnName("NAME")
    var name: String = "",

    @ColumnName("ICON")
    var icon: String = ""
) {
    override fun toString() =  "${if(parent == null)"" else "↳"}$name"
}

val NULL_DIRECTORY = Directory(name = "НЕТ")

data class GroupDirectory(
    var directory: Directory = Directory(),

    private var parent: GroupDirectory? = null,

    val reports: List<Report> = emptyList(),

    val childDirectories: MutableList<GroupDirectory> = ArrayList()
)

private const val SEQ_XLS_DIRECTORY = "select OD.SEQ_XLS_DIRECTORY.nextval from dual"