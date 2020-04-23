package ru.barabo.scanner.gui

import ru.barabo.db.EditType
import ru.barabo.db.service.StoreListener
import ru.barabo.gui.swing.groupPanel
import ru.barabo.gui.swing.maxSpaceYConstraint
import ru.barabo.gui.swing.textAreaHorizontal
import ru.barabo.gui.swing.textFieldHorizontal
import ru.barabo.scanner.entity.CashPay
import ru.barabo.scanner.service.CashPayService
import java.awt.GridBagLayout
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.JTextField

class PanelCashPay : JPanel(), StoreListener<List<CashPay>> {

    val payer: JTextField

    val payee: JTextField

    val inn: JTextField

    val kpp: JTextField

    val payeeAccount: JTextField

    val bik: JTextField

    val bankName: JTextField

    val description: JTextArea

    val amount: JTextField

    init {
        layout = GridBagLayout()

        groupPanel("Платеж", 0, 2, 0).apply {
            textFieldHorizontal("Сумма", 0).apply {
                amount = this
            }
        }

        groupPanel("Принято от", 2, 4, 0).apply {
            textFieldHorizontal("Плательщик", 0).apply {
                payer = this
            }

            textAreaHorizontal("Назначение", 1).apply {
                description = this
            }
        }

        groupPanel("Получатель", 6, 3, 0).apply {
            textFieldHorizontal("Наименование", 0, 0, 3).apply {
                payee = this
            }

            textFieldHorizontal("ИНН", 1).apply {
                inn = this
            }
            textFieldHorizontal("КПП", 1, 2).apply {
                kpp = this
            }
        }

        groupPanel("Реквизиты получателя", 9, 4, 0).apply {
            textFieldHorizontal("р/счет", 0).apply {
                payeeAccount = this
            }

            textFieldHorizontal("Бик банка", 1).apply {
                bik = this
            }

            textFieldHorizontal("Банк", 2).apply {
                bankName = this
            }
        }

        maxSpaceYConstraint(13)

        CashPayService.addListener(this)
    }

    override fun refreshAll(elemRoot: List<CashPay>, refreshType: EditType) {

        val entity = CashPayService.selectedEntity() ?: return

        fromEntity(entity)
    }

    private fun fromEntity(entity: CashPay) {
        payer.text = entity.payerFio

        payee.text = entity.payeeName

        inn.text = entity.payeeInn

        kpp.text = entity.payeeKpp

        payeeAccount.text = entity.payeeAccount

        bik.text = entity.payeeBik

        bankName.text = entity.payeeBankName

        description.text = entity.descriptionPay

        amount.text = entity.amount.toString()
    }
}