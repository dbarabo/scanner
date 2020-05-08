package ru.barabo.scanner.gui

import ru.barabo.gui.swing.table.ColumnTableModel
import ru.barabo.gui.swing.table.EntityTable
import ru.barabo.scanner.entity.CashPay
import ru.barabo.scanner.service.CashPayService

class TableCashPay : EntityTable<CashPay>(cashPayColumns, CashPayService)

private val cashPayColumns = listOf(
        ColumnTableModel("№ док-та", 50, CashPay::numberCashDoc, false),
        ColumnTableModel("Сумма", 70, CashPay::amountFormat, false),
        ColumnTableModel("ФИО", 150, CashPay::payerFio, false)
)