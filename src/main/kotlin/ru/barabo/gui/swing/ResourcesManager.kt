package ru.barabo.gui.swing

import java.io.InputStreamReader
import java.net.URL
import javax.swing.ImageIcon

object ResourcesManager {

    private val icoHash: HashMap<String, ImageIcon> = HashMap()

    var icoPath = "/ico/"

    @JvmStatic
    fun getIcon(icoName: String): ImageIcon? =
        icoHash[icoName] ?:  loadIcon(icoName)?.apply { icoHash[icoName] = this }

    private fun loadIcon(icoName :String): ImageIcon? = pathResource("$icoPath$icoName.png")?.let { ImageIcon(it) }

    private fun pathResource(fullPath: String): URL? {

        val path = ResourcesManager::class.java.getResource(fullPath)?.toExternalForm()

        return path?.let{ URL(it) }
    }

    private const val DB_FMS = "/fms.csv"

    private fun textFileLinesInJar(fullPath :String) =
        InputStreamReader(javaClass.getResourceAsStream(fullPath), "windows-1251").buffered().readLines()

    fun readFms() = textFileLinesInJar(DB_FMS)
}