package ru.barabo.scanner.service

import org.apache.log4j.Logger
import java.awt.KeyEventDispatcher
import java.awt.KeyboardFocusManager
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.KEY_LOCATION_UNKNOWN
import javax.swing.JTextArea

object ScannerDispatcher : KeyEventDispatcher {

    private val logger = Logger.getLogger(ScannerDispatcher::class.simpleName)!!

    private var scanText: String = ""

    private var isStart = false

    private var listeners: MutableList<ScanEventListener> = ArrayList()

    private lateinit var focusedComponent: JTextArea

    fun initKeyEvent(listener: ScanEventListener, focusedComp: JTextArea) {

        listeners.add(listener)

        focusedComponent = focusedComp

        isEnable = true
    }

    var isEnable: Boolean = false
        set(value) {
            if(value && !field) {
                onDispatcher()
            } else if(!value && field) {
                offDispatcher()
            }
            field = value
        }

    private fun offDispatcher() {
        logger.error("OFFFFF!!!!!!!!!!")
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this)
    }

    private fun onDispatcher() {
        logger.error("ON!!!!!!!!!!")
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this)
    }

    override fun dispatchKeyEvent(e: KeyEvent): Boolean {
        if(e.keyLocation != KEY_LOCATION_UNKNOWN && e.keyChar.toInt() !in listOf(FIRST_CHAR, END_CHAR)) return false

        if(!isStart) {
            if(e.keyChar.toInt() == FIRST_CHAR) {
                isStart = true
                scanText = ""
                focusedComponent.text = ""
                focusedComponent.isVisible = true
                focusedComponent.isEnabled = true
                focusedComponent.grabFocus()
                focusedComponent.parent.revalidate()
                focusedComponent.grabFocus()
            }
            return false
        }

        if(e.keyChar.toInt() == FIRST_CHAR) return false

        if(e.keyChar.toInt() == END_CHAR && scanText.isNotEmpty()) {
            val text = scanText
            scanText = ""
            isStart = false
            sendInfoToListeners(text)
            return false
        }
        //logger.error("e=$e")

        scanText += e.keyChar
        return false
    }

    private fun sendInfoToListeners(textInfo: String) {

        focusedComponent.text = ""
        logger.error("sendInfoToListeners =$textInfo")

        val mapInfo = textInfo.split('|')
                .filter { it.contains('=') }
                .map { it.substringBefore('=').toUpperCase().trim() to it.substringAfter('=') }
                .filter { it.first.isTagBarCode() && it.second.isNotcontainsKeys() }
                .toMap()


        listeners.forEach {
            it.scanInfo(mapInfo)
        }
    }
}

private fun String.isNotcontainsKeys(): Boolean {
    if(this.isBlank()) return true

    for(tag in ALL_TAGS) {
        if(this.contains(tag, true)) return false
    }
    return true
}

private fun String.isTagBarCode(): Boolean = TAGS.firstOrNull { it == this } != null

private val TAGS = arrayOf("PERSONALACC", "BIC", "NAME", "PAYEEINN", "PERSACC", "PURPOSE", "SUM",
        "LASTNAME", "FIRSTNAME", "MIDDLENAME", "PAYERADDRESS", "PERSACC", "PAYMPERIOD", "TAXPERIOD", "DOCNO",
        "CATEGORY", "KPP", "FIO", "BANKNAME")

private val ALL_TAGS = arrayOf("PERSONALACC", "BIC", "NAME", "PAYEEINN", "PERSACC", "PURPOSE", "SUM",
        "LASTNAME", "FIRSTNAME", "MIDDLENAME", "PAYERADDRESS", "PERSACC", "PAYMPERIOD", "TAXPERIOD", "DOCNO",
        "CATEGORY", "KPP", "FIO", "BANKNAME", "CORRESPACC", "PAYERADDRESS")

private const val FIRST_CHAR: Int = 65535

private const val END_CHAR: Int = 10

fun Map<String, String>.findAmount(): Double? = this["SUM"]?.toIntOrNull()?.div(100.0)

fun Map<String, String>.findFio(): String = this["FIO"]
        ?: this["LASTNAME"]?.let { "$it ${this["FIRSTNAME"]?:EMPTY} ${this["MIDDLENAME"]?:EMPTY}".trim() } ?: ""

fun Map<String, String>.findPayee(): String = this["NAME"]?:""

fun Map<String, String>.findPayeeInn(): String = this["PAYEEINN"]?:""

fun Map<String, String>.findPayeeKpp(): String = this["KPP"]?:""

fun Map<String, String>.findPayeeBic(): String = this["BIC"]?:""

fun Map<String, String>.findPayeeBankName(): String = this["BANKNAME"]?:""

fun Map<String, String>.findPayeeAccount(): String = this["PERSONALACC"]?:""

fun Map<String, String>.findDetailAccount(): String = this["PERSACC"] ?: this["DOCNO"] ?: ""

fun Map<String, String>.findDetailPeriod(): String = this["TAXPERIOD"] ?: this["PAYMPERIOD"] ?: ""

fun Map<String, String>.findDescription(): String {
    val desc = this["PURPOSE"] ?: this["CATEGORY"] ?: return EMPTY

    return if(desc.isBlank()) EMPTY else if(desc.contains("ОПЛАТА", true)) desc else "Оплата за $desc"
}

private const val EMPTY = ""

interface ScanEventListener {
    fun scanInfo(info: Map<String, String>)
}
