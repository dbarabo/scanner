package ru.barabo.report.service

import ru.barabo.afina.AfinaOrm
import ru.barabo.db.EditType
import ru.barabo.db.SessionSetting
import ru.barabo.db.annotation.ParamsSelect
import ru.barabo.db.service.StoreFilterService
import ru.barabo.db.service.StoreListener
import ru.barabo.report.entity.AccessReport
import ru.barabo.report.entity.Report

object AccessReportService : StoreFilterService<AccessReport>(AfinaOrm, AccessReport::class.java),
    ParamsSelect, StoreListener<List<Report>> {

    override fun selectParams(): Array<Any?>? = arrayOf(ReportService.selectedReport?.id ?: Long::class.javaObjectType)

    init {
        ReportService.addListener(this)
    }

    override fun refreshAll(elemRoot: List<Report>, refreshType: EditType) {

        initData()
    }

    override fun save(item: AccessReport, sessionSetting: SessionSetting): AccessReport {

        when {
            (!item.isAccess) && item.id != null -> {
                delete(item, sessionSetting)
                item.id = null
                initData()
            }

            item.isAccess && item.id == null -> {
                super.save(item, sessionSetting)
            }
        }
        return item
    }
}