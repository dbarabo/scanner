package ru.barabo.scanner.service

import ru.barabo.afina.rtf.RtfAfinaData

class RtfPayKinderGarden(private var idCash: Number) : RtfAfinaData {

    override fun procedureName(): String = "BBR_DOC_CASHINKO_1433U_318_14"

    override fun procedureCallSql(): String = "{ call BBR.BBR_DOC_CASHINKO_1433U_318_14(?) }"

    override fun paramCall(): Array<Any?>? = arrayOf(idCash)

    override fun bbrId(): Long = 1184525907L
}

class RtfPayCustomHouse(private var idCash: Number) : RtfAfinaData {

    override fun procedureName(): String = "BBR_DOC_CASHINKO_1433U_318_14"

    override fun procedureCallSql(): String = "{ call BBR.BBR_DOC_CASHINKO_1433U_318_14(?) }"

    override fun paramCall(): Array<Any?>? = arrayOf(idCash)

    override fun bbrId(): Long = 1202142395L
}