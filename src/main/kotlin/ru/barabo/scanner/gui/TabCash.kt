package ru.barabo.scanner.gui

import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.JSplitPane.HORIZONTAL_SPLIT

class TabCash : JPanel()  {

    init {
        layout = BorderLayout()

        add(ToolBarCash(), BorderLayout.NORTH)

        val mainHorizontalSplit = JSplitPane(HORIZONTAL_SPLIT, PanelCashPay(), JScrollPane(TableCashPay()) ).apply {
            resizeWeight = 0.75
        }

        add(mainHorizontalSplit, BorderLayout.CENTER)
    }

    companion object {
        const val TITLE = "Сканер"
    }
}