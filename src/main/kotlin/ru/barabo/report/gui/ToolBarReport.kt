package ru.barabo.report.gui

import ru.barabo.afina.AccessMode
import ru.barabo.afina.AfinaQuery
import ru.barabo.gui.swing.menuItem
import ru.barabo.gui.swing.popupButton
import ru.barabo.gui.swing.processShowError
import ru.barabo.gui.swing.toolButton
import ru.barabo.report.service.DirectoryService
import ru.barabo.report.service.ReportService
import java.lang.Exception
import javax.swing.JToolBar

class ToolBarReport : JToolBar() {
    init {
        toolButton("refresh", "Обновить") { refreshData() }

        popupButton("Создать ➧", "newFile") {
            menuItem("Папку", "folder") {
                DialogCreateDirectory(null, this).showDialogResultOk()
            }

            menuItem("Отчет", "exportXLS") {
                DialogCreateReport(null, this).showDialogResultOk()
            }
        }.apply {
            isEnabled = AfinaQuery.getUserDepartment().accessMode == AccessMode.FullAccess

            isVisible = isEnabled
        }

        popupButton("Правка ➧", "application") {
            menuItem("Папки", "folder") {
                DialogCreateDirectory(DirectoryService.selectedDirectory?.directory, this).showDialogResultOk()
            }

            menuItem("Отчета", "exportXLS") {
                DialogCreateReport(ReportService.selectedReport, this).showDialogResultOk()
            }
        }.apply {
            isEnabled = AfinaQuery.getUserDepartment().accessMode == AccessMode.FullAccess

            isVisible = isEnabled
        }

        val access = toolButton("readonly", "Доступы") { showAccess() }

        access?.isEnabled = AfinaQuery.getUserDepartment().accessMode == AccessMode.FullAccess
        access?.isVisible = access!!.isEnabled
    }

    private fun refreshData() {
        DirectoryService.initData()
    }

    private fun showAccess() {
        processShowError {
            if(ReportService.selectedReport?.id == null) throw Exception("Сначала выберите отчет для установки для него доступов")

            DialogAccessReport(this).showDialogResultOk()
        }
    }
}