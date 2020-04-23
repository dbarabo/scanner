package ru.barabo.report.service

import ru.barabo.afina.AfinaOrm
import ru.barabo.afina.AfinaQuery
import ru.barabo.db.EditType
import ru.barabo.db.annotation.ParamsSelect
import ru.barabo.db.service.StoreFilterService
import ru.barabo.db.service.StoreListener
import ru.barabo.report.entity.Remark
import ru.barabo.report.entity.Report

object RemarkService : StoreFilterService<Remark>(AfinaOrm, Remark::class.java),
    ParamsSelect, StoreListener<List<Report>> {

    override fun selectParams(): Array<Any?>? = arrayOf(ReportService?.selectedReport?.id ?: Long::class.javaObjectType)

    init {
        ReportService.addListener(this)
    }

    override fun refreshAll(elemRoot: List<Report>, refreshType: EditType) {

        initData()
    }

    fun addInfo(info: String?, isHide: Boolean) {
        if(info.isNullOrBlank()) return

        val remark = Remark(report = ReportService.selectedReport?.id,
            remarker = AfinaQuery.getUserDepartment().userId,
            isHide = if(isHide)1L else 0L,
            remarkerName = AfinaQuery.getUserDepartment().userName ?: AfinaQuery.getUserDepartment().userId,
            remark = info)

        save(remark)
    }

    fun fullInfo(): String {

        val userId = AfinaQuery.getUserDepartment().userId

        return dataList.filter { it.state in listOf(0L, 1L) &&(it.isHide == 0L || it.remarker == userId) }
            .joinToString("\n\n") { it.remarkInfo() }
    }
}

