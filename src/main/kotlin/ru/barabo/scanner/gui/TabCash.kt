package ru.barabo.scanner.gui

import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.JSplitPane.HORIZONTAL_SPLIT

class TabCash : JPanel()  {

    init {
        layout = BorderLayout()

        val panelCashPay = PanelCashPay()

        add(ToolBarCash(panelCashPay), BorderLayout.NORTH)

        val mainHorizontalSplit = JSplitPane(HORIZONTAL_SPLIT, panelCashPay, JScrollPane(TableCashPay()) ).apply {
            resizeWeight = 0.8

            this.isOneTouchExpandable = true
        }

        add(mainHorizontalSplit, BorderLayout.CENTER)
    }

    companion object {
        const val TITLE = "Сканер"
    }
}