package ru.barabo.scanner.gui

import ru.barabo.db.EditType
import ru.barabo.db.service.StoreListener
import ru.barabo.gui.swing.onOffButton
import ru.barabo.gui.swing.processShowError
import ru.barabo.gui.swing.toolButton
import ru.barabo.scanner.entity.CashPay
import ru.barabo.scanner.service.CashPayService
import ru.barabo.scanner.service.ClientPhysicService
import ru.barabo.scanner.service.ScanEventListener
import ru.barabo.scanner.service.ScannerDispatcher
import javax.swing.AbstractButton
import javax.swing.JCheckBox
import javax.swing.JTextArea
import javax.swing.JToolBar

class ToolBarCash(private val panelCashPay: PanelCashPay) : JToolBar(), StoreListener<List<CashPay>>,
    ScanEventListener {

//    private val logger = LoggerFactory.getLogger(ToolBarCash::class.java)!!

    private val execButton: AbstractButton

    private val deleteButton: AbstractButton

    private var onOffButton: JCheckBox

    private val scannerText: JTextArea = JTextArea().apply {
        rows = 2
        isEditable = true

        isEnabled = false
    }

    init {
        ScannerDispatcher.initKeyEvent(CashPayService, scannerText)

        ScannerDispatcher.addScanEventListener(this)

        onOffButton("Режим Сканера", true) {
            ScannerDispatcher.isEnable = !ScannerDispatcher.isEnable

            scannerText.requestFocus()

            panelCashPay.refreshAllDefault()

        }.apply {
            panelCashPay.isScanOnOff = this
            onOffButton = this

            this.focusTraversalKeysEnabled = false

            this.focusTraversalPolicy
        }

        toolButton("newFile24", "Новый платеж") {

            panelCashPay.refreshAllDefault()

            CashPayService.newPay()

            scannerText.requestFocus()
        }

        toolButton("save24", "Сохранить") {
            processShowError {
                CashPayService.savePay()
            }
            scannerText.requestFocus()
        }

        toolButton("exec24", "Исполнить") {
            processShowError {
                CashPayService.savePay()
                CashPayService.execPay()
            }
            scannerText.requestFocus()
        }.apply {
            execButton = this
        }

        addSeparator()

        toolButton("deleteDB24", "Удалить") {
            processShowError {
                CashPayService.removePay()
            }
            scannerText.requestFocus()
        }.apply {
            deleteButton = this
        }

        toolButton("print24", "Печать") {
            processShowError {
                CashPayService.print()
            }
            scannerText.requestFocus()
        }

        addSeparator()

        toolButton("refresh24", "Обновить клиентов") {
            processShowError {
                ClientPhysicService.reselectAllData( CashPayService.selectedEntity() )
            }
            scannerText.requestFocus()
        }

        add(scannerText)

        CashPayService.addListener(this)

        scannerText.isEnabled = false
    }

    override fun refreshAll(elemRoot: List<CashPay>, refreshType: EditType) {

        execButton.isEnabled = CashPayService.selectedEntity()?.state in arrayOf(0L, -1L)
    }

    override fun scanInfo(info: Map<String, String>) {
        if((!info["FIO"].isNullOrEmpty()) &&
            (!info["PAYER_DOC_NUMBER"].isNullOrEmpty()) &&
            (!info["PAYER_DOC_LINE"].isNullOrEmpty())  ) {

            panelCashPay.isScanOnOff.isSelected = false
            ScannerDispatcher.isEnable = false
        }
    }
}