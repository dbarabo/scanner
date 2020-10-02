package ru.barabo.scanner.service

import org.apache.log4j.Logger
import java.awt.KeyEventDispatcher
import java.awt.KeyboardFocusManager
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.*
import javax.swing.JTextArea
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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

        if(e.keyLocation == KEY_LOCATION_NUMPAD) return false
        //if(e.keyLocation != KEY_LOCATION_UNKNOWN) return false

        if(e.keyLocation == KEY_LOCATION_UNKNOWN && focusedComponent != KeyboardFocusManager.getCurrentKeyboardFocusManager().focusOwner) {
            focusedComponent.grabFocus()
            focusedComponent.text = ""
            focusedComponent.isEnabled = false
            focusedComponent.parent.revalidate()
        }

        if(e.keyChar.toInt() == 65535) return false
        if(e.id != KEY_TYPED) return false
        // logger.error("${e.keyChar}_INT=${e.keyChar.toInt()}${e.paramString()}")

        if(e.keyChar.toInt() == ENTER_CHAR && scanText.isNotEmpty()) {
            val text = scanText
            scanText = ""
            focusedComponent.text = ""
            isStart = false
            sendInfoToListeners(text)
        }
        scanText += e.keyChar
        return false
/*
        if(e.keyLocation != KEY_LOCATION_UNKNOWN && e.keyChar.toInt() !in listOf(FIRST_CHAR, ENTER_CHAR)) return false

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

        if(e.keyChar.toInt() == ENTER_CHAR && scanText.isNotEmpty()) {
            val text = scanText
            scanText = ""
            isStart = false
            sendInfoToListeners(text)
            return false
        }

        scanText += e.keyChar
        return

 */
    }

    private fun sendInfoToListeners(textInfo: String) {

        focusedComponent.text = ""
        logger.error("sendInfoToListeners =$textInfo")

        val mapInfo = textInfo.split('|')
                .filter { it.contains('=') }
                .map { it.substringBefore('=').toUpperCase().trim() to it.substringAfter('=') }
                .filter { it.first.isTagBarCode() && it.second.isNotcontainsKeys() }
                .toMap()

        logger.error("mapInfo =$mapInfo")

        //val mapInfo = parseScanCodes(textInfo)

        listeners.forEach {
            it.scanInfo(mapInfo)
        }
    }

    private fun parseScanCodes(textInfo: String): Map<String, String> {

        val tagValues = textInfo.split('=')
        if(tagValues.size < 2) return emptyMap()

        val (data, nextCorrTag)  = parseScanDefaultCodes(tagValues)
        if(tagValues.size < 7) return data


        for((key, value) in data) {
            logger.error("data key=$key")
            logger.error("data value=$value")
        }

        val tempMap = HashMap<String, String>()

        var tag = nextCorrTag.trim().toUpperCase()
        for(tagValue in tagValues.drop(6)) {
            val (nameValue, nextTag, tagTypeValue) = parseTagValue(tagValue)

            logger.error("CALC key=$tag")
            logger.error("CALC value=$nameValue")

            if(TAGS.contains(tag)) {
                data[tag] = nameValue
            } else {
                val parseTagPrepare = prepareParseTag(nameValue, tagTypeValue, tag, data)
                if(parseTagPrepare != null) {
                    data[parseTagPrepare] = nameValue

                    logger.error("CALC PARSE TAG=$parseTagPrepare")
                } else { // only digits
                    tempMap[tag] = nameValue
                }
            }
            tag = nextTag.trim().toUpperCase()
        }

        if(tempMap.isNotEmpty()) {
            endParseTempDigits(tempMap, data)
        }

        return data
    }

    private fun prepareParseTag(nameValue: String, tagType: TypeTagValue, tagName: String, data: Map<String, String>): String? {
        return when(tagType) {
            TypeTagValue.DIGIT_ONLY -> tryPrepareParseDigit(nameValue, tagName, data)
            TypeTagValue.DIGIT_DOT -> if(nameValue.length in 3..10) "TAXPERIOD" else "IGNORE"
            TypeTagValue.LETTER_ANY_WITH_COMMA -> if(nameValue.length > 10 && data["PAYERADDRESS"] == null) "PAYERADDRESS" else "IGNORE"
            TypeTagValue.LETTER_SERVICE -> if(data["PURPOSE"] == null) "PURPOSE" else "IGNORE"
            TypeTagValue.LETTER_DOT_SPACE2MAX -> if(data["FIO"] == null) "FIO" else "IGNORE"
            TypeTagValue.LETTER_DOT -> when {
                    data["LASTNAME"] == null -> "LASTNAME"
                    data["FIRSTNAME"] == null -> "FIRSTNAME"
                    data["MIDDLENAME"] == null -> "MIDDLENAME"
                    else -> "IGNORE"
                }
            else -> "IGNORE"
        }
    }

    private fun endParseTempDigits(tempDigits: HashMap<String, String>, data: HashMap<String, String>) {

        if(data["SUM"] == null) {
            tryFindSum(tempDigits, data)
        }
        if(data["PERSACC"] == null) {
            tryFindPersAccount(tempDigits, data)
        }
    }

    private fun tryFindSum(tempDigits: HashMap<String, String>, data: HashMap<String, String>) {
        for((key, value) in tempDigits.entries) {
            if(value.length < 8 && value[0] != '0' && key.length < 4) {
                data["SUM"] = value
                tempDigits.remove(key)
                return
            }
        }
    }

    private fun tryFindPersAccount(tempDigits: Map<String, String>, data: HashMap<String, String>) {
        for((key, value) in tempDigits.entries) {
            if(value.length > 4 && key.length > 3) {
                data["PERSACC"] = value
                return
            }
        }
    }

    private fun tryPrepareParseDigit(nameValue: String, tagName: String, data: Map<String, String>): String? {
        if((nameValue.length == 10 || nameValue.length == 12) && (data["PAYEEINN"] == null) && nameValue[0] != '0') {
            return "PAYEEINN"
        }

        if(nameValue.trim().toInt() <= 99) {
            return if(tagName.indexOf("PERIOD") >= 0) "PAYMPERIOD" else "IGNORE"
        }

        if(nameValue[0] == '0' && data["PERSACC"] == null) {
            return "PERSACC"
        }

        if(data["SUM"] == null && nameValue.length < 8 && tagName.length < 4 && nameValue[0] != '0' &&
            (tagName.indexOf('S') == 0 || tagName.indexOf('U') >= 0) || tagName.indexOf('M') >= 0) {
            return "SUM"
        }

        if(data["KPP"] == null && nameValue.length == 9 && tagName.length < 4
                && nameValue[0] != '0' && nameValue[6] == '0' && nameValue[7] == '0') {
            return "KPP"
        }

        return null
    }

    private fun parseScanDefaultCodes(tagValues: List<String>): Pair<HashMap<String, String>, String> {
        val data = HashMap<String, String>()
        val (nameValue, nextTag) = parseName(tagValues[1])
        data["NAME"] = nameValue

        if(tagValues.size < 3) return Pair(data, "")
        val (account, tagAccount, tagTypeAccount) = parseTagValue(tagValues[2])
        data["PERSONALACC"] = if(tagTypeAccount == TypeTagValue.DIGIT_ONLY) account else ""

        if(tagValues.size < 4) return Pair(data, "")
        val (bankName, tagBankName, tagTypeBankName) = parseTagValue(tagValues[3])
        data["BANKNAME"] = if(tagTypeBankName != TypeTagValue.DIGIT_ONLY) bankName else ""

        if(tagValues.size < 5) return Pair(data, "")
        val (bik, tagBik, tagTypeBik) = parseTagValue(tagValues[4])
        data["BIC"] = if(tagTypeBik == TypeTagValue.DIGIT_ONLY) bik else ""

        if(tagValues.size < 6) return Pair(data, "")
        val (correspAcc, tagNextCorrespAcc, tagTypeCorrespAcc) = parseTagValue(tagValues[5])
        data["CORRESPACC"] = if(tagTypeCorrespAcc == TypeTagValue.DIGIT_ONLY) correspAcc else ""

        return Pair(data, tagNextCorrespAcc)
    }
}

enum class TypeTagValue {
    EMPTY,
    DIGIT_ONLY,
    DIGIT_DOT,
    LETTER_DOT,
    LETTER_DOT_SPACE2MAX,
    LETTER_ANY_WITH_COMMA,
    LETTER_SERVICE,
    LETTER_OTHERS
}

private fun parseTagValue(valueTag: String): Triple<String, String, TypeTagValue> {
    val (separatorIndex, typeTag) = afterLastSymbol(valueTag)

    val (value, tagName) = valueTag.valueAndTag(separatorIndex)

    return Triple(value, tagName, typeTag)
}

private fun parseName(valueTag: String): Pair<String, String> {

    val separatorIndex = afterLastStringSymbol(valueTag)

    return valueTag.valueAndTag(separatorIndex)
}

private fun String.valueAndTag(separatorIndex: Int): Pair<String, String> {
    val value = if(separatorIndex > 0) this.substring(0 until separatorIndex) else ""

    val nextTag = if(separatorIndex < this.length) this.substring(separatorIndex + 1) else ""

    return Pair(value, nextTag)
}

private fun afterLastSymbol(valueTag: String): Pair<Int, TypeTagValue> {
    if(valueTag.isEmpty()) return Pair(0, TypeTagValue.EMPTY)

    return if(valueTag[0].isDigit()) afterLastDigitSymbol(valueTag) else afterLastStringDigitSymbol(valueTag)
}

private fun afterLastDigitSymbol(valueTag: String): Pair<Int, TypeTagValue> {

    var tagType = TypeTagValue.DIGIT_ONLY

    for((index, symbol) in valueTag.withIndex()) {
        if(symbol == '.') {
            tagType = TypeTagValue.DIGIT_DOT
            continue
        }

        if(!('0'..'9').contains(symbol)){
            return Pair(index, tagType)
        }
    }

    return Pair(valueTag.length, tagType)
}

private fun afterLastStringDigitSymbol(valueTag: String): Pair<Int, TypeTagValue> {

    val separatorIndex = afterLastStringSymbol(valueTag)

    val tagType = tagTypeLetter(valueTag, separatorIndex)

    return Pair(separatorIndex, tagType)
}

private fun tagTypeLetter(valueTag: String, separatorIndex: Int): TypeTagValue {
    val (value, tagName) = valueTag.valueAndTag(separatorIndex)

    if(value.indexOf(',') > 0) return TypeTagValue.LETTER_ANY_WITH_COMMA

    val words = value.split(' ', '.')

    if(words.isService()) return TypeTagValue.LETTER_SERVICE

    if(!words.isLetterCyrilic()) return TypeTagValue.LETTER_OTHERS

    if(words.size == 1) return TypeTagValue.LETTER_DOT

    return if(words.size > 3) TypeTagValue.LETTER_OTHERS else TypeTagValue.LETTER_DOT_SPACE2MAX
}

private val SERVICE_WORDS = arrayOf("ЭЛЕКТРОЭНЕРГИ", "УСЛУГ", "ЖКХ", "ЖКУ", "ОПЛАТА", "КАПРЕМОНТ", "ДОМОФОН")

private fun List<String>.isService(): Boolean {
    for(word in this) {
        if(word.isService()) return true
    }
    return false
}

private fun String.isService(): Boolean {
    if(this.isEmpty()) return false

    for(service in SERVICE_WORDS) {
        if(this.toUpperCase().indexOf(service) >= 0) return true
    }

    return false
}

private fun List<String>.isLetterCyrilic(): Boolean {
    if(this.isEmpty()) return false

    for(word in this) {
        if(!word.isLetterCyrilic()) return false
    }
    return true
}

private fun String.isLetterCyrilic(): Boolean {
    if(this.isEmpty()) return false

    for(symbol in this) {
        if(!('А'..'я').contains(symbol)) return false
    }
    return true
}

private fun afterLastStringSymbol(valueTag: String): Int {
    for((index, symbol) in valueTag.withIndex()) {
        if(!('А'..'я').contains(symbol) &&
            !listOf('ё', 'Ё', ' ', '.', '"', ',', '\'', '№').contains(symbol) &&
            !('0'..'9').contains(symbol) ) {

            return index
        }
        if(index > 0 && (symbol == 'Ь' || symbol == 'Ж') && (valueTag[index-1] != valueTag[index-1].toUpperCase())) {
            return index
        }
    }
    return valueTag.length
}

private fun String.isNotcontainsKeys(): Boolean {
    if(this.isBlank()) return true

    for(tag in ALL_TAGS) {
        if(this.contains(tag, true)) return false
    }
    return true
}

private fun String.isTagBarCode(): Boolean = TAGS.firstOrNull { it == this } != null

private val TAGS = arrayOf(
    "PERSONALACC", "BIC", "NAME", "PAYEEINN", "PERSACC", "PURPOSE", "SUM",
    "LASTNAME", "FIRSTNAME", "MIDDLENAME", "PAYERADDRESS", "PERSACC", "PAYMPERIOD", "TAXPERIOD", "DOCNO",
    "CATEGORY", "KPP", "FIO", "BANKNAME"
)

private val ALL_TAGS = arrayOf(
    "PERSONALACC", "BIC", "NAME", "PAYEEINN", "PERSACC", "PURPOSE", "SUM",
    "LASTNAME", "FIRSTNAME", "MIDDLENAME", "PAYERADDRESS", "PERSACC", "PAYMPERIOD", "TAXPERIOD", "DOCNO",
    "CATEGORY", "KPP", "FIO", "BANKNAME", "CORRESPACC", "PAYERADDRESS"
)

private const val FIRST_CHAR: Int = 65535

private const val ENTER_CHAR: Int = 10

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
