package ru.barabo.scanner.gui

import ru.barabo.db.service.StoreFilterService
import ru.barabo.gui.swing.cross.*
import ru.barabo.gui.swing.table.ColumnTableModel
import ru.barabo.gui.swing.table.EntityTable
import ru.barabo.scanner.entity.CashPay
import ru.barabo.scanner.service.CashPayService
import java.awt.Color
import java.awt.Component
import java.awt.Font
import java.awt.font.TextAttribute
import javax.swing.JLabel
import javax.swing.JTable
import javax.swing.UIManager
import javax.swing.table.TableCellRenderer

class TableCashPay : EntityTable<CashPay>(cashPayColumns, CashPayService) {

    private val renderer: TableCellRenderer

    init {
        renderer = CashPayRenderer()
    }

    override fun getCellRenderer(row: Int, column: Int): TableCellRenderer? = renderer
}

private val cashPayColumns = listOf(
        ColumnTableModel("№ док-та", 50, CashPay::numberCashDoc, false),
        ColumnTableModel("Сумма", 70, CashPay::amountFormat, false),
        ColumnTableModel("ФИО", 150, CashPay::payerFio, false)
)

private class CashPayRenderer : JLabel(), TableCellRenderer {

    override fun getTableCellRendererComponent(table: JTable, value: Any?,
                                               isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component? {

        isOpaque = true
        checkBackForeground(table, isSelected, hasFocus, row, column)

        val state = CashPayService.getEntity(row)?.state

        background = when(state) {
            0L -> table.background
            1L -> Color.LIGHT_GRAY
            2L -> Color.RED
            -1L -> Color.BLUE
            else -> table.background
        }

        text = value?.toString() ?: ""

        return this
    }

    private fun checkBackForeground(table: JTable, hasFocus: Boolean, isSelected: Boolean, row: Int, column: Int) {
        if (hasFocus) {
            border = UIManager.getBorder("Table.focusCellHighlightBorder")
            if (table.isCellEditable(row, column)) {
                foreground = UIManager.getColor("Table.focusCellForeground")
                background = UIManager.getColor("Table.focusCellBackground")
            }
        } else {
            border = UIManager.getBorder("TableHeader.cellBorder")
        }

        if (isSelected) {
            background = table.selectionBackground
            foreground = table.selectionForeground
        } else {
            background = table.background
            foreground = table.foreground
        }
    }
}

val MORE_LIGHT_GRAY = Color(243, 243, 243)

val MORE_GRAY_BACK = Color(238, 238, 238)