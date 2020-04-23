package ru.barabo.report.gui

import ru.barabo.gui.swing.AbstractDialog
import ru.barabo.gui.swing.table.ColumnTableModel
import ru.barabo.gui.swing.table.EntityTable
import ru.barabo.gui.swing.toolButton
import ru.barabo.report.entity.AccessReport
import ru.barabo.report.service.AccessReportService
import ru.barabo.report.service.ReportService
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JScrollPane
import javax.swing.JToolBar

class DialogAccessReport(component: Component) : AbstractDialog(component, "Установка доступов для отчета") {

    init {
        layout = BorderLayout()

        add(JScrollPane(TableAccess), BorderLayout.CENTER)

        add( ToolBarAccessReport {
            dispose()
        }, BorderLayout.NORTH)

        packWithLocation()
    }

    override fun okProcess() {}
}

class ToolBarAccessReport(disposeDialog: ()->Unit) : JToolBar() {
    init {
        toolButton("endFilter", "Закрыть") {
            try { TableAccess.cellEditor.stopCellEditing() } catch (e: Exception) {}

            disposeDialog()
        }

        add(JLabel("${ReportService.selectedReport?.name}"))
    }
}

object TableAccess : EntityTable<AccessReport>(accessColumns, AccessReportService)

private val accessColumns = listOf (
    ColumnTableModel("Доступ", 10, AccessReport::isAccess, true),
    ColumnTableModel("Группа доступа", 60, AccessReport::groupName, false),
    ColumnTableModel("Тип группы", 30, AccessReport::groupTypeName, false)
)