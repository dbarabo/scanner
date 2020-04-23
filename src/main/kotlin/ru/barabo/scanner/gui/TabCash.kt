package ru.barabo.scanner.gui

import java.awt.BorderLayout
import javax.swing.JPanel

class TabCash : JPanel()  {



    init {
        layout = BorderLayout()

        add(ToolBarCash(), BorderLayout.NORTH)

        add(PanelCashPay(), BorderLayout.CENTER)
    }

    companion object {
        const val TITLE = "Сканер"
    }
}