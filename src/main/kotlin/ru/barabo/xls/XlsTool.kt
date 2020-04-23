package ru.barabo.xls

import jxl.Workbook
import jxl.write.WritableWorkbook
import org.slf4j.LoggerFactory
import java.io.File

private val logger = LoggerFactory.getLogger(ExcelSql::class.java)

fun createNewBook(newFile: File, templateFile: File): WritableWorkbook {

    var templateBook: Workbook? = null

    try {
        templateBook = Workbook.getWorkbook(templateFile)

        val newBook = Workbook.createWorkbook(newFile, templateBook)

        templateBook.close()

        return newBook
    } catch (e: Exception) {

        logger.error("createNewBook", e)

        templateBook?.close()

        throw Exception(e.message)
    }
}

fun WritableWorkbook.save() {
    write()

    close()
}

