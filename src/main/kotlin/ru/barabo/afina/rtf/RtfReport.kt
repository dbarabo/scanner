package ru.barabo.afina.rtf

import oracle.jdbc.OracleTypes
import ru.barabo.afina.AfinaQuery
import ru.barabo.afina.clobToString
import ru.barabo.cmd.Cmd
import ru.barabo.gui.swing.getDefaultToDirectory
import java.io.File
import java.nio.charset.Charset
import java.sql.Clob
import javax.swing.JFileChooser

interface RtfAfinaData {
    fun procedureName(): String

    fun procedureCallSql(): String

    fun paramCall(): Array<Any?>?

    fun bbrId(): Long?

    fun buildRtfReport(): File {

        val folderTo = defaultDirectory("rtf")

        val textFile = buildReportTextData(this, folderTo)

        val rtfTemplate = loadRftTemplate(this, folderTo)

        val rtfReportFile = getReportRtfFile(this.procedureName(), folderTo)

        try {
            executeConverter(textFile, rtfTemplate, rtfReportFile)
        } catch (e: java.lang.Exception) {
            textFile.delete()
            rtfReportFile.delete()
            rtfTemplate.delete()

            throw Exception(e)
        }

        return rtfReportFile
    }
}

private fun executeConverter(textFile: File, rtfTemplate: File, outRtf: File) {

    prepareConverter(textFile.parentFile)

    val cmd = cmdConverterXp(textFile, rtfTemplate, outRtf)

    Cmd.execCmd(cmd, textFile.parentFile)
}

private fun cmdConverterXp(textFile: File, rtfTemplate: File, outRtf: File): String =
        "${fullPathConverterExe(textFile.parentFile)} ${textFile.name} ${rtfTemplate.name} ${outRtf.name} /p"

private fun prepareConverter(directoryTo: File) {
    val toConverter = fullPathConverterExe(directoryTo)

    if (toConverter.exists()) {
        return
    }

    val sourceConverter: File = fullPathConverterExe(File(Cmd.LIB_FOLDER))
    if (!sourceConverter.exists()) {
        sourceConverter.parentFile.mkdirs()

        File(MAIN_LIB_CONVERTER).copyTo(sourceConverter, true)
    }
    if (!sourceConverter.exists()) throw Exception("file not found $MAIN_LIB_CONVERTER")

    sourceConverter.copyTo(toConverter, true)
}

private fun getReportRtfFile(fileName: String,  directoryTo: File) =
        File("${directoryTo.absolutePath}/$fileName${System.currentTimeMillis()}.rtf")

private fun buildReportTextData(rtfAfinaData: RtfAfinaData, directoryRtf: File): File {

    val textFile = File("${directoryRtf.absolutePath}/${rtfAfinaData.procedureName()}.txt")

    val data = getBbrText(rtfAfinaData.procedureName(), rtfAfinaData.procedureCallSql(), rtfAfinaData.paramCall())
            ?: throw Exception("Данные в отчете ${rtfAfinaData.procedureName()} не найдены")

    textFile.writeText(data.clobToString(), Charset.forName("cp1251"))

    return textFile
}

private fun loadRftTemplate(rtfAfinaData: RtfAfinaData, directoryRtf: File): File {

    val rftTemplate = File("${directoryRtf.absolutePath}/${rtfAfinaData.procedureName()}.rtf")

//    if (rftTemplate.exists()) {
//        return rftTemplate
//    }

    AfinaQuery.selectBlobToFile(SELECT_RTF_TEMPLATE, arrayOf(rtfAfinaData.bbrId()), rftTemplate)

    if(!rftTemplate.exists()) throw Exception("файл шаблона не загружен из АБС $rftTemplate")

    return rftTemplate
}

private fun getBbrText(procedureName: String, query: String, params :Array<Any?>?): Clob? {
    val settings = AfinaQuery.uniqueSession()

    val data = try {
        val outValues = AfinaQuery.execute(PREPARE_BBR,
                arrayOf(procedureName), settings, intArrayOf(OracleTypes.NUMBER, OracleTypes.NUMBER, OracleTypes.NUMBER))

        AfinaQuery.execute(query, params, settings, null)

        AfinaQuery.execute(REPORT_DATA, outValues?.toTypedArray(), settings, intArrayOf(OracleTypes.CLOB) )?.apply {
            AfinaQuery.commitFree(settings)
        }
    } catch (e: Exception) {
        AfinaQuery.rollbackFree(settings)

        throw e
    }

    return if(data.isNullOrEmpty() ) null else data[0] as? Clob
}

private fun fullPathConverterExe(directory: File): File = File("${directory.absolutePath}/$CONVERT_EXE")

private const val MAIN_LIB = "\\\\192.168.0.35\\work2\\Modules\\java\\lib\\"

private const val CONVERT_EXE = "Convert.exe"

private const val MAIN_LIB_CONVERTER = "$MAIN_LIB$CONVERT_EXE"

private const val SELECT_RTF_TEMPLATE = "select PartData from od.BlankFilePart where BlankCmd = ? order by OrderNum"

private const val PREPARE_BBR = "{ call od.PTKB_PLASTIC_AUTO.setReportId(?, ?, ?, ?) }"

private const val REPORT_DATA = "{ call od.PTKB_PLASTIC_AUTO.getReportData(?, ?, ?, ?) }"

private fun defaultDirectory(dirName: String): File {
    val directory = File("${getDefaultToDirectory().absolutePath}/$dirName")

    if(!directory.exists()) {
        directory.mkdirs()
    }

    return directory
}

private fun getDefaultToDirectory(): File = JFileChooser().fileSystemView.defaultDirectory

