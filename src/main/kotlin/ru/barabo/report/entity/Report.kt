package ru.barabo.report.entity

import ru.barabo.afina.AfinaQuery
import ru.barabo.afina.SEQ_CLASSIFIED
import ru.barabo.db.annotation.*
import ru.barabo.gui.swing.getDefaultToDirectory
import java.io.File
import java.sql.Timestamp
import java.util.*

@SelectQuery("""select r.id, r.directory, r.state, r.name, r.template_name, 
  r.version_id, r.creator, r.created, r.updater, r.updated,
  (select count(*) from od.xls_history_run h where h.report = r.id) COUNT_,
(select count(*) 
  from dual where (1000005945 = ?)
  or exists (select *
from od.workplaceaccess wa
join od.accessgrouptowp aw on aw.workplace = wa.workplace
join od.xls_report_access ra on ra.access_group = aw.accessgroup
where wa.userid = user
  and wa.VALIDFROMDATE < sysdate and sysdate < wa.VALIDTODATE
  and ra.report = r.id )
) IS_ACCESS

from od.xls_report r
where r.directory = ?
  and (r.state = 1 or 1000005945 = ?)
order by r.id""")
@TableName("OD.XLS_REPORT")
data class Report (
    @ColumnName("ID")
    @SequenceName(SEQ_CLASSIFIED)
    @ColumnType(java.sql.Types.BIGINT)
    var id: Long? = null,

    @ColumnName("DIRECTORY")
    @ColumnType(java.sql.Types.BIGINT)
    var directory: Long? = null,

    @ColumnName("STATE")
    var state: Long = 0,

    @ColumnName("NAME")
    var name: String = "",

    @ColumnName("TEMPLATE_NAME")
    var fileName: String = "",

    @ColumnName("VERSION_ID")
    var versionId: Long = 0,

    @ColumnName("CREATOR")
    var creator: String = "",

    @ColumnName("UPDATER")
    var updater: String = "",

    @ColumnName("CREATED")
    var created: Timestamp = Timestamp(Date().time),

    @ColumnName("UPDATED")
    var updated: Timestamp = Timestamp(Date().time),

    @ColumnName("COUNT_")
    @ReadOnly
    var count: Long = 0,

    @ColumnName("IS_ACCESS")
    @ReadOnly
    var accessed: Long = 0L,

    var templateFile: File? = null
) {
    val isAccess: Boolean
    get() = (accessed != 0L)

    val nameWithCount: String
    get() = "$name ($count)"

    fun getTemplate(saveDirectory: File = defaultTemplateDirectory() ): File {
        if(id == null || fileName.isBlank()) throw Exception("must be report.id is not null and report.template is not empty")

        templateFile = File("$saveDirectory/$fileName")

        return AfinaQuery.selectBlobToFile(SELECT_BLOB_TEMPLATE_REPORT, arrayOf(id), templateFile!!)
    }

    fun uploadFile() {
        if(id == null ||templateFile?.exists() != true) throw Exception("must be exists template file $templateFile")

        AfinaQuery.execute(UPDATE_BLOB_BY_FILE, arrayOf(templateFile, id))
    }

    fun change(nameReport: String?, directory: Directory, state: StateReport, uploadFile: File? = null) {

        this.name = nameReport ?: throw Exception("Наименование отчета должно быть заполнено")

        uploadFile?.let {
            if(!it.exists()) throw Exception("не найден файл шаблона $it")

            templateFile = it
            fileName = it.name

            uploadFile()
        }

        this.directory = directory.id
        this.state = state.dbValue
    }
}

private const val UPDATE_BLOB_BY_FILE = "update OD.XLS_REPORT r set TEMPLATE = ? where r.id = ?"

private const val SELECT_BLOB_TEMPLATE_REPORT = "select r.TEMPLATE from OD.XLS_REPORT r where r.id = ?"

fun defaultTemplateDirectory(): File = defaultDirectory("temp")

fun defaultReportDirectory(): File = defaultDirectory("xls")

private fun defaultDirectory(dirName: String): File {
    val directory = File("${getDefaultToDirectory().absolutePath}/$dirName")

    if(!directory.exists()) {
        directory.mkdirs()
    }

    return directory
}

enum class StateReport(val label: String, val dbValue: Long) {
    NEW("В Разработке", 0),
    WORK("Действующий", 1),
    OLD("Устарел", 2);

    override fun toString(): String = label

    companion object {
        fun findByDbValue(dbValue: Long): StateReport? = values().firstOrNull { it.dbValue == dbValue }
    }
}
