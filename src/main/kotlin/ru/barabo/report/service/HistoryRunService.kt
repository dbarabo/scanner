package ru.barabo.report.service

import org.apache.log4j.Logger
import ru.barabo.afina.AfinaOrm
import ru.barabo.afina.AfinaQuery
import ru.barabo.db.EditType
import ru.barabo.db.annotation.ParamsSelect
import ru.barabo.db.service.StoreFilterService
import ru.barabo.db.service.StoreListener
import ru.barabo.report.entity.HistoryRun
import ru.barabo.report.entity.Report
import ru.barabo.report.entity.defaultReportDirectory
import java.io.File
import java.lang.Exception
import java.sql.Timestamp
import java.time.format.DateTimeFormatter
import java.util.*

object HistoryRunService : StoreFilterService<HistoryRun>(AfinaOrm, HistoryRun::class.java),
    ParamsSelect, StoreListener<List<Report>> {

    private val logger = Logger.getLogger(HistoryRunService::class.simpleName)!!

    private var lastCreatedhistoryRun: HistoryRun? = null

    override fun selectParams(): Array<Any?>? = arrayOf(
        ReportService?.selectedReport?.id ?: Long::class.javaObjectType,
        AfinaQuery.getUserDepartment().workPlaceId
    )

    init {
        ReportService.addListener(this)
    }

    override fun refreshAll(elemRoot: List<Report>, refreshType: EditType) {
        initData()
    }

    fun createHistoryRun(report: Report): HistoryRun {
        val now = Timestamp(Date().time)

        val fullFileName = fileByTemplate(report.templateFile!!, now)

        lastCreatedhistoryRun = HistoryRun(report = report.id, versionId = report.versionId,
            runner = AfinaQuery.getUserDepartment().userId, runned = now, fileName = fullFileName,
            runnerName = AfinaQuery.getUserDepartment().userName?:"",
            workPlaceName = AfinaQuery.getUserDepartment().workPlace?:"",
            workPlaceId = AfinaQuery.getUserDepartment().workPlaceId)

        return lastCreatedhistoryRun!!
    }

    fun addHistoryByFile(fileReport: File) {
        if(isLastFind(fileReport) ) {
            save(lastCreatedhistoryRun!!)
            lastCreatedhistoryRun = null
        }
    }

    fun addErrorHistory(error: String, reportFile: File?) {
        if(isLastFind(reportFile) ) {

            lastCreatedhistoryRun?.error = error
            save(lastCreatedhistoryRun!!)
            lastCreatedhistoryRun = null
        }
    }

    private fun isLastFind(fileReport: File?): Boolean {
        val lastFileName =  lastCreatedhistoryRun?.fileName
            ?.let {it.substringAfterLast('/', it.substringAfterLast('\\') ) }
            ?: throw Exception("Не найден последний файл")

        return lastFileName == fileReport?.name
    }
}

private fun fileByTemplate(template: File, time: Timestamp)=
    "${defaultReportDirectory()}/${template.nameWithoutExtension}-${time.formatFile()}.xls"

private fun Timestamp.formatFile(): String = DateTimeFormatter.ofPattern ("MM-dd-HH%mm%ss").format(this.toLocalDateTime())

