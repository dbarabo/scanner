package ru.barabo.scanner.gui

import ru.barabo.db.EditType
import ru.barabo.db.service.StoreListener
import ru.barabo.gui.swing.onOffButton
import ru.barabo.gui.swing.processShowError
import ru.barabo.gui.swing.toolButton
import ru.barabo.scanner.entity.CashPay
import ru.barabo.scanner.service.CashPayService
import ru.barabo.scanner.service.ScannerDispatcher
import javax.swing.AbstractButton
import javax.swing.JTextArea
import javax.swing.JToolBar

class ToolBarCash(panelCashPay: PanelCashPay) : JToolBar(), StoreListener<List<CashPay>> {

    private val execButton: AbstractButton

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

        toolButton("exec24", "Исполнить") {
            processShowError {
                CashPayService.execPay()
            }
        }.apply {
            execButton = this!!
        }

        add(scannerText)

        CashPayService.addListener(this)
    }

    override fun refreshAll(elemRoot: List<CashPay>, refreshType: EditType) {

        execButton.isEnabled = CashPayService.selectedEntity()?.state == 0L
    }
}