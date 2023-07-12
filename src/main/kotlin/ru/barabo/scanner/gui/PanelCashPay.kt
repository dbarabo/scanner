package ru.barabo.scanner.gui

import org.jdesktop.swingx.JXDatePicker
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator
import org.slf4j.LoggerFactory
import ru.barabo.db.EditType
import ru.barabo.db.service.StoreFilterService
import ru.barabo.db.service.StoreListener
import ru.barabo.gui.swing.*
import ru.barabo.scanner.entity.CashPay
import ru.barabo.scanner.entity.ClientPhysic
import ru.barabo.scanner.entity.PactDepartment
import ru.barabo.scanner.entity.PasportType
import ru.barabo.scanner.service.CashPayService
import ru.barabo.scanner.service.ClientPhysicService
import ru.barabo.scanner.service.PactDepartmentService
import ru.barabo.scanner.service.PasportTypeService
import java.awt.*
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.sql.Timestamp
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.swing.*
import javax.swing.text.*
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.jvm.javaType


private val logger = LoggerFactory.getLogger(PanelCashPay::class.java)!!

private const val DATE_FORMAT = "dd.MM.yyyy"

private const val ANY_DATE_FORMAT = "ddMMyyyy"

private val DATE_FORMATTER = SimpleDateFormat(DATE_FORMAT)

private const val TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.S"

private val TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT)

private val ANY_DATE_FORMATTER = DateTimeFormatter.ofPattern(ANY_DATE_FORMAT)

private fun String.timestampToFormat(formatter: DateTimeFormatter): Timestamp =
    Timestamp.valueOf( LocalDate.parse(this, formatter).atStartOfDay() )

fun dateFromTextTimestamp(dateText: String?): Timestamp? {

    if(dateText.isNullOrBlank()) return null

    return dateText.takeIf { dateText.length == TIMESTAMP_FORMAT.length }?.timestampToFormat(TIMESTAMP_FORMATTER)
        ?: dateText.filter { it.isDigit() }.takeIf { it.length == ANY_DATE_FORMAT.length }?.timestampToFormat(ANY_DATE_FORMATTER)
}

fun JXDatePicker.setDateFromText(dateText: String) {
    date = dateFromTextTimestamp(dateText)
}

fun JXDatePicker.getDateAsText(): String? =
    date?.let { TIMESTAMP_FORMATTER.format(it.toInstant().atZone(ZoneId.systemDefault())) }

class ClientPhysicListener(private val combo: JComboBox<ClientPhysic>) : StoreListener<List<ClientPhysic>> {
    override fun refreshAll(elemRoot: List<ClientPhysic>, refreshType: EditType) {
        if(refreshType != EditType.INIT) return

        updateAllItems()
    }

    private fun updateAllItems() {

        val payerIndex = CashPayService.selectedEntity()?.payerId?.let { ClientPhysicService.getIndexListById(it) }

        val listeners = combo.actionListeners
        listeners.forEach {  combo.removeActionListener(it) }

        combo.model = DefaultComboBoxModel( ClientPhysicService.elemRoot().toTypedArray() )

        payerIndex?.let {
            combo.selectedIndex = payerIndex
        }
        listeners.forEach { combo.addActionListener(it) }
    }
}

class PanelCashPay : JPanel(), StoreListener<List<CashPay>>  {

    private val assignerProps = ArrayList<AssignerProp<CashPay>>()

    private lateinit var pasportTypeCombo: JComboBox<PasportType>

    private lateinit var pactCombo: JComboBox<PactDepartment>

    lateinit var outDoc: JComboBox<String>

    lateinit var isScanOnOff: JCheckBox

    private lateinit var codeDepartment: JTextField

    private lateinit var clientPhysicListener: ClientPhysicListener

    init {
        layout = GridBagLayout()

        //defaultPayView()
        customOnlyPayView()

        maxSpaceYConstraint(21)

        setEnabledAll(false)

        CashPayService.addListener(this)

        ClientPhysicService.addListener(clientPhysicListener)
    }

    private fun customOnlyPayView() {

        groupPanel("Плательщик", 0, 3, 0).apply {
            comboBoxWithItems(label = "Фамилия Имя Отчество", gridY = 0, list = ClientPhysicService.elemRoot() ).apply {

                first.isEditable = true

                AutoCompleteDecorator.decorate(first)

                first.isEnabled = true
                first.maximumRowCount = 20

                assignerProps += AssignerProp(first.editor.editorComponent, CashPay::payerFio, CashPayService::selectedEntity,
                    first::setSelectedItem, { first.selectedItem?.toString() } )

                first.addActionListener { setSelectedPayerCombo( first.selectedIndex ) }

                first.editor.editorComponent.addFocusListener(PayerFocusListener(first.editor.editorComponent as JTextComponent, this@PanelCashPay) )

                clientPhysicListener = ClientPhysicListener(first)
            }
            textFieldHorizontal("ИНН", 0, 2).apply {
                assignerProps += AssignerProp(this, CashPay::payerInn, CashPayService::selectedEntity,
                    this::setText, { this.text } )
            }

            textFieldHorizontal("Адрес", 1, 0, 3).apply {
                assignerProps += AssignerProp(this, CashPay::payerAddress, CashPayService::selectedEntity,
                    this::setText, { this.text } )
            }
        }

        groupPanel("Паспорт РФ", 3, 4, 0).apply {
            textFieldHorizontal("Серия", 0).apply {
                assignerProps += AssignerProp(this, CashPay::linePasport, CashPayService::selectedEntity,
                    this::setText, { this.text } )
            }
            textFieldHorizontal("Номер", 0, 2).apply {
                assignerProps += AssignerProp(this, CashPay::numberPasport, CashPayService::selectedEntity,
                    this::setText, { this.text } )
            }
            datePicker("Дата выдачи", 0, 4).apply {
                val cusFormater = CustomDateFormatter()
                val field = JFormattedTextField(cusFormater)
                field.inputVerifier = FieldVerifier()
                this.editor = field

                assignerProps += AssignerProp(this.editor, CashPay::dateIssued, CashPayService::selectedEntity,
                    this::setDateFromText
                ) { this.editor.text }

                //this.setFormats( DATE_FORMATTER )
            }

            textFieldHorizontal("Код подразделения", 1, 0).apply {
                assignerProps += AssignerProp(this, CashPay::departmentCode, CashPayService::selectedEntity,
                    this::setText, { this.text } )

                codeDepartment = this

                codeDepartment.minimumSize = Dimension(110, codeDepartment.minimumSize.height)

                codeDepartment.preferredSize = Dimension(110, codeDepartment.preferredSize.height)
            }
            comboBoxChangeItems<String>("Кем выдан", 1, gridX = 2, width = 3).apply {

                isEditable = true

                assignerProps += AssignerProp(editor.editorComponent, CashPay::byIssued, CashPayService::selectedEntity,
                    this::setSelectedItem, { this.selectedItem?.toString() } )

                codeDepartment.addKeyListener(CodeOutKeyListener(CashPay::departmentCode,
                    CashPayService::selectedEntity,this@apply) )

                outDoc = this

                outDoc.preferredSize = Dimension((Toolkit.getDefaultToolkit().screenSize.width/2.2).toInt(), outDoc.preferredSize.height)
            }

            datePicker("Дата рождения", 2, 0).apply {
                val cusFormater = CustomDateFormatter()
                val field = JFormattedTextField(cusFormater)
                field.inputVerifier = FieldVerifier()
                this.editor = field

                assignerProps += AssignerProp(this.editor, CashPay::birthDate, CashPayService::selectedEntity,
                    this::setDateFromText
                ) { this.editor.text }
            }
            textFieldHorizontal("Место рождения", 2, 2, 3).apply {
                assignerProps += AssignerProp(this, CashPay::birthPlace, CashPayService::selectedEntity,
                    this::setText, { this.text } )
            }
        }

        groupPanel("Платеж", 7, 6, 0).apply {
            textFieldHorizontal("№ Таможни", 0, 0).apply {
                assignerProps += AssignerProp(this, CashPay::detailPeriod, CashPayService::selectedEntity,
                    this::setText, { this.text } )
            }
            comboBox("Договор", 0, PactDepartmentService.elemRoot(), 2, width = 3).apply {

                pactCombo = this

                maximumRowCount = 20

                addActionListener { setSelectedPactCombo( selectedIndex ) }
            }

            textFieldHorizontal("Сумма", 1, 0, 1).apply {

                assignerProps += AssignerProp(this, CashPay::amount, CashPayService::selectedEntity,
                    this::setText, { this.text } )
            }
            textFieldHorizontal("Назначение платежа", 1, 2, 3).apply {
                isEditable = true

                assignerProps += AssignerProp(this, CashPay::descriptionPay, CashPayService::selectedEntity,
                    this::setText, { this.text } )
            }

            textFieldHorizontal("№ Док-та", 2).apply {

                isEditable = false

                assignerProps += AssignerProp(this, CashPay::numberCashDoc, CashPayService::selectedEntity,
                    this::setText, { this.text } )
            }
            textFieldHorizontal("Статус", 2, 2).apply {
                isEditable = false

                assignerProps += AssignerProp(this, CashPay::stateName, CashPayService::selectedEntity,
                    this::setText, { this.text } )
            }
            textFieldHorizontal("Время", 2, 4).apply {
                isEditable = false

                assignerProps += AssignerProp(this, CashPay::timeDoc, CashPayService::selectedEntity,
                    this::setText, { this.text } )
            }
        }

        logger.error("outDoc.maximumSize=${outDoc.maximumSize}")
        logger.error("outDoc.w=${outDoc.width}")
        logger.error("outDoc.h=${outDoc.height}")


        val screenSize = Toolkit.getDefaultToolkit().screenSize.width

        logger.error("screenSize=$screenSize")
    }

    private fun defaultPayView() {
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

            textFieldHorizontal("Таможня/Период опл.", 4, 2).apply {
                assignerProps += AssignerProp(this, CashPay::detailPeriod, CashPayService::selectedEntity,
                    this::setText, { this.text } )
            }
        }

        groupPanel("Принято от", 6, 7, 0).apply {

            comboBoxWithItems(label = "Плательщик", gridY = 0, list = ClientPhysicService.elemRoot() ).apply {

                first.isEditable = true

                AutoCompleteDecorator.decorate(first)

                first.isEnabled = true
                first.maximumRowCount = 20

                assignerProps += AssignerProp(first.editor.editorComponent, CashPay::payerFio, CashPayService::selectedEntity,
                    first::setSelectedItem, { first.selectedItem?.toString() } )

                first.addActionListener { setSelectedPayerCombo( first.selectedIndex ) }

                clientPhysicListener = ClientPhysicListener(first)
           }

            textFieldHorizontal("ИНН Плательщика", 0, 2).apply {
                assignerProps += AssignerProp(this, CashPay::payerInn, CashPayService::selectedEntity,
                    this::setText, { this.text } )
            }

            textFieldHorizontal("Адрес", 1, 0, 3).apply {
                assignerProps += AssignerProp(this, CashPay::payerAddress, CashPayService::selectedEntity,
                    this::setText, { this.text } )
            }

            comboBox("Тип док-та", 2, PasportTypeService.elemRoot()).apply {

                pasportTypeCombo = this

                addActionListener { setSelectedPassportTypeCombo( selectedIndex ) }
            }

            textFieldHorizontal("Код подразделения", 2, 2).apply {
                assignerProps += AssignerProp(this, CashPay::departmentCode, CashPayService::selectedEntity,
                    this::setText, { this.text } )

                codeDepartment = this
            }

            textFieldHorizontal("Серия", 3).apply {
                assignerProps += AssignerProp(this, CashPay::linePasport, CashPayService::selectedEntity,
                    this::setText, { this.text } )
            }

            textFieldHorizontal("Номер", 3, 2).apply {
                assignerProps += AssignerProp(this, CashPay::numberPasport, CashPayService::selectedEntity,
                    this::setText, { this.text } )
            }

            comboBoxChangeItems<String>("Кем выдан", 4).apply {

                isEditable = true

                assignerProps += AssignerProp(editor.editorComponent, CashPay::byIssued, CashPayService::selectedEntity,
                    this::setSelectedItem, { this.selectedItem?.toString() } )

                codeDepartment.addKeyListener(CodeOutKeyListener(CashPay::departmentCode,
                    CashPayService::selectedEntity,this@apply) )
            }

            datePicker("Дата док-та", 4, 2).apply {
                assignerProps += AssignerProp(this.editor, CashPay::dateIssued, CashPayService::selectedEntity,
                    this::setDateFromText
                ) { this.editor.text }

                this.setFormats( DATE_FORMATTER )
            }

            textFieldHorizontal("Место рождения", 5).apply {
                assignerProps += AssignerProp(this, CashPay::birthPlace, CashPayService::selectedEntity,
                    this::setText, { this.text } )
            }
            datePicker("Дата рождения", 5, 2).apply {
                assignerProps += AssignerProp(this.editor, CashPay::birthDate, CashPayService::selectedEntity,
                    this::setDateFromText
                ) { this.editor.text }

                this.setFormats( DATE_FORMATTER )
            }
        }

        groupPanel("Получатель", 13, 4, 0).apply {
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

        groupPanel("Реквизиты получателя", 17, 4, 0).apply {
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
    }

    fun refreshAllDefault() {
        refreshAll(emptyList<CashPay>(), EditType.ALL)
    }

    override fun refreshAll(elemRoot: List<CashPay>, refreshType: EditType) {
        fromEntity()

        val isEditable = (CashPayService.selectedEntity()?.state == 0L) && (!isScanOnOff.isSelected)

        setEnabledAll(isEditable)
    }

    fun fromEntity() {
        assignerProps.forEach { it.valueFromProp() }

        if(::pasportTypeCombo.isInitialized) {
            pasportTypeCombo.selectedIndex = PasportTypeService.selectedRowIndex
        }

        pactCombo.selectedIndex = PactDepartmentService.selectedRowIndex
    }

    private fun setSelectedPayerCombo( comboIndex: Int ) {

        val cashPay = CashPayService.selectedEntity() ?: return

        ClientPhysicService.setSelectedNewIndex(comboIndex)
        if(comboIndex < 0 || comboIndex != ClientPhysicService.selectedRowIndex ) return

        ClientPhysicService.updatePayDocument(cashPay)
        fromEntity()

        CashPayService.infoUpdatedPayer()
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
            Timestamp::class.javaObjectType -> dateFromTextTimestamp(value)
            else -> {
                try {
                    value?.replace(',', '.')?.trim()?.toDouble() ?: 0.0
                } catch (e: Exception) {
                    0.0
                }
            }
        }

        (prop as KMutableProperty1<E, Any?>).set(entity, typeValue)
    }
}

fun <T> Container.comboBoxChangeItems(label: String, gridY: Int, list: List<T>? = null, gridX: Int = 0, width: Int = 1): JComboBox<T> {

    add( JLabel(label), labelConstraint(gridY, gridX) )

    //this.maximumSize

    val items = list?.let { Vector(it) }

    val combo = items?.let { JComboBox(it) } ?: JComboBox()

    add(combo, textConstraint(gridY = gridY, gridX = gridX + 1, width = width) )

    return combo
}

class CodeOutKeyListener<E>(private val prop: KMutableProperty1<E, out Any?>,
                         private val entityGetter: ()->E?,
                         private val combo: JComboBox<String>) : KeyListener {

    private var priorCode: String = ""
    override fun keyReleased(e: KeyEvent?) {

        val entity = entityGetter() ?: return
        val value = prop.getter(entity)?.toString() ?: return

        if(value == priorCode) return

        priorCode = value

        setNewItems()
    }

    private fun setNewItems() {

        val items: Array<String> = if(priorCode.isBlank()) emptyArray<String>()
            else CashPayService.getFmsByCode(priorCode.trim()).toTypedArray()

        combo.model = DefaultComboBoxModel(items)

        if(items.size == 1) {
          combo.selectedIndex = 0
        }
     }

    override fun keyTyped(e: KeyEvent?) {}

    override fun keyPressed(e: KeyEvent?) {}
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

class PayerFocusListener(private val text: JTextComponent, private val panel: PanelCashPay) : FocusListener {

    override fun focusLost(e: FocusEvent?) {
        if(CashPayService.selectedEntity() != null &&
            ClientPhysicService.isExistsClearDataIfNotEquals(CashPayService.selectedEntity()!!, text.text) ){

            CashPayService.selectedEntity()!!.payerFio = text.text
            panel.fromEntity()
        }
    }

    override fun focusGained(e: FocusEvent?) {}
}

private fun Container.setEnabledAll(isEnabl: Boolean) {

    for(child in this.components) {

        if(child.isEnabled != isEnabl) {
            child.isEnabled = isEnabl
        }

        if(child is Container) {
            child.setEnabledAll(isEnabl)
        }
    }
}

internal class CustomDateFormatter : DefaultFormatter() {
    val df: DateFormat

    init {
        df = SimpleDateFormat("dd.MM.yyyy")
    }

    override fun getDocumentFilter(): DocumentFilter = CustomDateFilter(formattedTextField)

    override fun valueToString(value: Any?): String {
        return if (value != null) df.format(value) else ""
    }

    override fun stringToValue(text: String?): Any? {
        var date: Date? = null
        try {
            date = df.parse(text)
        } catch (pe: ParseException) {

        }
        return date
    }
}


internal class CustomDateFilter(var ftf: JFormattedTextField) : DocumentFilter() {
    var validChars = "0123456789."
    var day = 2
    var month = 2
    var year = 4
    var sep = '.'
    var maxLength: Int

    init {
        maxLength = day + sep.toString().length +
                month + sep.toString().length + year
    }

    @Throws(BadLocationException::class)
    override fun insertString(
        fb: FilterBypass,
        offset: Int,
        str: String,
        attrs: AttributeSet?
    ) {
        replace(fb, offset, 0, str, attrs)
    }

    @Throws(BadLocationException::class)
    override fun replace(
        fb: FilterBypass,
        offset: Int,
        length: Int,
        str: String,
        attrs: AttributeSet?
    ) {
        var str = str
        val document: Document = fb.document
        val textLength: Int = document.length
        val start = ftf.selectionStart
        val end = ftf.selectionEnd
        val selectionLength = end - start
        if (textLength + str.length - selectionLength > maxLength) {
            Toolkit.getDefaultToolkit().beep()
            str = str.substring(0, maxLength - textLength + selectionLength)
        }
        val source = str.toCharArray()
        val result = CharArray(source.size + 2 * sep.toString().length)
        var k = 0
        for (j in source.indices) {
            if (isSepNext(offset, j)) result[k++] = sep
            if (validChars.indexOf(source[j]) != -1) result[k++] = source[j] else Toolkit.getDefaultToolkit().beep()
        }
        fb.replace(offset, length, String(result, 0, k), attrs)
    }

    private fun isSepNext(offset: Int, j: Int): Boolean {
        val pos = offset + j
        return pos == month || pos == month + 1 + day
    }
}


internal class FieldVerifier : InputVerifier() {
    var sep = "."
    override fun verify(input: JComponent): Boolean {
        val ftf = input as JFormattedTextField
        val s = ftf.text
        if (s.indexOf(sep) == 2 && s.lastIndexOf(sep) == 5 && s.length == 10) return true
        Toolkit.getDefaultToolkit().beep()
        return false
    }

    fun shouldYeildFocus(input: JComponent): Boolean {
        return verify(input)
    }
}