package ru.barabo.scanner.gui

import org.apache.log4j.Logger
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator
import ru.barabo.db.EditType
import ru.barabo.db.service.StoreFilterService
import ru.barabo.db.service.StoreListener
import ru.barabo.gui.swing.*
import ru.barabo.scanner.entity.CashPay
import ru.barabo.scanner.entity.PactDepartment
import ru.barabo.scanner.entity.PasportType
import ru.barabo.scanner.service.CashPayService
import ru.barabo.scanner.service.ClientPhysicService
import ru.barabo.scanner.service.PactDepartmentService
import ru.barabo.scanner.service.PasportTypeService
import java.awt.Component
import java.awt.Container
import java.awt.GridBagLayout
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.JComboBox
import javax.swing.JPanel
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.jvm.javaType

private val logger = Logger.getLogger(PanelCashPay::class.simpleName)!!

class PanelCashPay : JPanel(), StoreListener<List<CashPay>> {

    private val assignerProps = ArrayList<AssignerProp<CashPay>>()

    private val pasportTypeCombo: JComboBox<PasportType>

    private val pactCombo: JComboBox<PactDepartment>

    init {
        layout = GridBagLayout()

        groupPanel("Платеж", 0, 6, 0).apply {
            textFieldHorizontal("№", 0).apply {
                isEditable = false

                assignerProps += AssignerProp(this, CashPay::numberCashDoc, CashPayService::selectedEntity,
                        this::setText, { this.text } )
            }

            textFieldHorizontal("Статус", 0, 2).apply {
                isEditable = false

                assignerProps += AssignerProp(this, CashPay::stateName, CashPayService::selectedEntity,
                        this::setText, { this.text } )
            }

            textFieldHorizontal("Сумма", 1, 0).apply {

                assignerProps += AssignerProp(this, CashPay::amount, CashPayService::selectedEntity,
                         this::setText, { this.text } )
            }

            textFieldHorizontal("Счет 409", 1, 2).apply {
                isEditable = false

                assignerProps += AssignerProp(this, CashPay::payAccount, CashPayService::selectedEntity,
                        this::setText, { this.text } )
            }

            textAreaHorizontal("Назначение платежа", 2, 2, 3).apply {
                isEditable = true

                assignerProps += AssignerProp(this, CashPay::descriptionPay, CashPayService::selectedEntity,
                        this::setText, { this.text } )
            }

            textFieldHorizontal("Л/С плательщика", 4, 0).apply {

                assignerProps += AssignerProp(this, CashPay::detailAccount, CashPayService::selectedEntity,
                        this::setText, { this.text } )
            }

            textFieldHorizontal("Период оплаты", 4, 2).apply {
                assignerProps += AssignerProp(this, CashPay::detailPeriod, CashPayService::selectedEntity,
                        this::setText, { this.text } )
            }
        }

        groupPanel("Принято от", 6, 6, 0).apply {

            comboBoxWithItems("Плательщик", 0, ClientPhysicService.elemRoot(), 0, 3).apply {

                first.isEditable = true

                AutoCompleteDecorator.decorate(first)

                first.isEnabled = true
                first.maximumRowCount = 20

                assignerProps += AssignerProp(first.editor.editorComponent, CashPay::payerFio, CashPayService::selectedEntity,
                        first::setSelectedItem, { first.selectedItem?.toString() } )

                first.addActionListener { setSelectedPayerCombo( first.selectedIndex ) }
            }

            comboBox("Тип док-та", 1, PasportTypeService.elemRoot()).apply {

                pasportTypeCombo = this

                addActionListener { setSelectedPassportTypeCombo( selectedIndex ) }
            }

            textFieldHorizontal("Код подразделения", 1, 2).apply {
                assignerProps += AssignerProp(this, CashPay::departmentCode, CashPayService::selectedEntity,
                        this::setText, { this.text } )
            }

            textFieldHorizontal("Серия", 2).apply {
                assignerProps += AssignerProp(this, CashPay::linePasport, CashPayService::selectedEntity,
                        this::setText, { this.text } )
            }

            textFieldHorizontal("Номер", 2, 2).apply {
                assignerProps += AssignerProp(this, CashPay::numberPasport, CashPayService::selectedEntity,
                        this::setText, { this.text } )
            }

            textFieldHorizontal("Кем выдан", 3).apply {
                assignerProps += AssignerProp(this, CashPay::byIssued, CashPayService::selectedEntity,
                        this::setText, { this.text } )
            }
            datePicker("Дата док-та", 3, 2).apply {
                // assignerProps += AssignerProp(this, CashPay::linePasport, CashPayService::selectedEntity,
                //         this::setText, { this.text } )
            }

            textFieldHorizontal("Место рождения", 4).apply {
                assignerProps += AssignerProp(this, CashPay::birthPlace, CashPayService::selectedEntity,
                        this::setText, { this.text } )
            }
            datePicker("Дата рождения", 4, 2).apply {
                // assignerProps += AssignerProp(this, CashPay::linePasport, CashPayService::selectedEntity,
                //         this::setText, { this.text } )
            }
        }

        groupPanel("Получатель", 12, 4, 0).apply {
            textFieldHorizontal("Наименование", 0, 0, 3).apply {
                assignerProps += AssignerProp(this, CashPay::payeeName, CashPayService::selectedEntity,
                        this::setText, { this.text } )
            }

            textFieldHorizontal("ИНН", 1).apply {
                assignerProps += AssignerProp(this, CashPay::payeeInn, CashPayService::selectedEntity,
                        this::setText, { this.text } )
            }

            textFieldHorizontal("КПП", 1, 2).apply {
                assignerProps += AssignerProp(this, CashPay::payeeKpp, CashPayService::selectedEntity,
                        this::setText, { this.text } )
            }

            comboBox("Договор", 2, PactDepartmentService.elemRoot(), 0, 3).apply {

                pactCombo = this

                maximumRowCount = 20

                addActionListener { setSelectedPactCombo( selectedIndex ) }
            }
        }

        groupPanel("Реквизиты получателя", 16, 4, 0).apply {
            textFieldHorizontal("р/счет", 0).apply {
                assignerProps += AssignerProp(this, CashPay::payeeAccount, CashPayService::selectedEntity,
                        this::setText, { this.text } )
            }

            textFieldHorizontal("Бик банка", 1).apply {
                assignerProps += AssignerProp(this, CashPay::payeeBik, CashPayService::selectedEntity,
                        this::setText, { this.text } )
            }

            textFieldHorizontal("Банк", 2).apply {
                assignerProps += AssignerProp(this, CashPay::payeeBankName, CashPayService::selectedEntity,
                        this::setText, { this.text } )
            }
        }

        maxSpaceYConstraint(20)

        setEnabledAll(false)

        CashPayService.addListener(this)
    }

    override fun refreshAll(elemRoot: List<CashPay>, refreshType: EditType) {
        fromEntity()
    }

    private fun fromEntity() {
        assignerProps.forEach { it.valueFromProp() }

        pasportTypeCombo.selectedIndex = PasportTypeService.selectedRowIndex

        pactCombo.selectedIndex = PactDepartmentService.selectedRowIndex
    }

    private fun setSelectedPayerCombo( comboIndex: Int ) {

        val cashPay = CashPayService.selectedEntity() ?: return

        ClientPhysicService.setSelectedNewIndex(comboIndex) ?: return
        ClientPhysicService.updatePayDocument(cashPay)
        fromEntity()
    }

    private fun setSelectedPactCombo( comboIndex: Int ) {

        val cashPay = CashPayService.selectedEntity() ?: return

        val oldPact = PactDepartmentService.selectedEntity()

        PactDepartmentService.setSelectedNewIndex(comboIndex) ?: return

        PactDepartmentService.updatePayDocument(oldPact, cashPay)
        fromEntity()
    }

    private fun setSelectedPassportTypeCombo( comboIndex: Int ) {

        val cashPay = CashPayService.selectedEntity() ?: return

        val pasportType = PasportTypeService.setSelectedNewIndex(comboIndex) ?: return

        cashPay.pasportTypeName = pasportType.label
        cashPay.typePasport = pasportType.id
        cashPay.isResident = pasportType.isResident ?: cashPay.isResident
    }
}

private fun <T: Any> StoreFilterService<T>.setSelectedNewIndex(newIndex: Int): T? {
    if(newIndex < 0) return null

    val oldSelectedEntity = this.selectedRowIndex
    this.selectedRowIndex = newIndex

    if(oldSelectedEntity == this.selectedRowIndex) return null

    return this.selectedEntity()
}

class AssignerProp <E> (component: Component,
                      private val prop: KMutableProperty1<E, out Any?>,
                      private val entityGetter: ()->E?,
                      private val setter: (String)-> Unit,
                      private val getter: ()->String?) {

    init {

        component.addKeyListener(PropKeyListener(this))

        component.addFocusListener( PropFocusListener(this) )
    }

    fun valueFromProp() {
        val entity = entityGetter() ?: return
        val value = prop.getter(entity)?.toString() ?: ""

        setter(value)
    }

    fun valueToProp() {
        val entity = entityGetter() ?: return

        val value = getter()

        val javaType = prop.returnType.javaType as Class<*>

        val typeValue = when(javaType) {
            String::class.javaObjectType -> value ?: ""
            else -> {
                try {
                    value?.replace(',', '.')?.trim()?.toDouble() ?: 0.0
                } catch (e: Exception) {
                    0.0
                }
            }
        }

        (prop as KMutableProperty1<E, Any>).set(entity, typeValue)
    }
}

class PropKeyListener<E>(private val assignerProp: AssignerProp<E>) : KeyListener {
    override fun keyReleased(e: KeyEvent?) {

        assignerProp.valueToProp()
    }

    override fun keyTyped(e: KeyEvent?) {}

    override fun keyPressed(e: KeyEvent?) {}
}

class PropFocusListener<E>(private val assignerProp: AssignerProp<E>) : FocusListener {

    override fun focusLost(e: FocusEvent?) {
        assignerProp.valueToProp()
    }

    override fun focusGained(e: FocusEvent?) {}
}

fun Container.setEnabledAll(isEnabled: Boolean) {

    for(child in this.components) {

        child.isEnabled = isEnabled

        if(child is Container) {
            child.setEnabledAll(isEnabled)
        }
    }
}
