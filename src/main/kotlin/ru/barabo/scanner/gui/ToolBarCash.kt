package ru.barabo.scanner.gui

import ru.barabo.gui.swing.onOffButton
import ru.barabo.gui.swing.toolButton
import ru.barabo.scanner.service.CashPayService
import ru.barabo.scanner.service.ScannerDispatcher
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.JToolBar

class ToolBarCash : JToolBar() {


    private val scannerText: JTextArea = JTextArea().apply {
        rows = 2
        isEditable = true
    }

    init {
        ScannerDispatcher.initKeyEvent(CashPayService, scannerText)

        onOffButton("Режим Сканера", true){
            ScannerDispatcher.isEnable = !ScannerDispatcher.isEnable
        }.apply {
            this.focusTraversalKeysEnabled = false

            this.focusTraversalPolicy
        }

        toolButton("save24", "Сохранить") {
            CashPayService.saved()
        }

       add(scannerText)
    }
}