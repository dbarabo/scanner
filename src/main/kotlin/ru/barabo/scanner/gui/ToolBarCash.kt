package ru.barabo.scanner.gui

import ru.barabo.gui.swing.onOffButton
import ru.barabo.gui.swing.processShowError
import ru.barabo.gui.swing.toolButton
import ru.barabo.scanner.service.CashPayService
import ru.barabo.scanner.service.ScannerDispatcher
import javax.swing.JTextArea
import javax.swing.JToolBar

class ToolBarCash(panelCashPay: PanelCashPay) : JToolBar() {


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

        toolButton("newFile24", "Новый платеж") {
            panelCashPay.setEnabledAll(true)

            CashPayService.newPay()
        }

        toolButton("save24", "Сохранить") {
            processShowError {
                CashPayService.savePay()
            }
        }

       add(scannerText)
    }
}