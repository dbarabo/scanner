package ru.barabo.scanner.main

import ru.barabo.afina.AfinaQuery
import ru.barabo.afina.VersionChecker
import ru.barabo.afina.gui.ModalConnect
import ru.barabo.gui.swing.ResourcesManager
import ru.barabo.gui.swing.processShowError
import ru.barabo.report.gui.TabReport
import ru.barabo.scanner.gui.TabCash
import java.awt.BorderLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame
import javax.swing.JTabbedPane
import kotlin.system.exitProcess

fun main() {

    Scanner()
}

class Scanner : JFrame() {

    init {
        runConnect()
    }

    private fun runConnect() {
        if (!ModalConnect.initConnect(this)) {
            exitProcess(0)
        }

        var isOk = false

        processShowError {
            AfinaQuery.execute(query = CHECK_WORKPLACE, params = null)

            isOk = true
        }

        if (!isOk) {
            exitProcess(0)
        }

        buildGui()
    }

    private fun buildGui() {

        layout = BorderLayout()

        title = title()
        iconImage = ResourcesManager.getIcon("scanner")?.image

        add( buildMainBook(), BorderLayout.CENTER)

        defaultCloseOperation = EXIT_ON_CLOSE
        isVisible = true

        pack()
        extendedState = MAXIMIZED_BOTH

        VersionChecker.runCheckVersion("SCANNER.JAR", 0)

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                VersionChecker.exitCheckVersion()
            }
        })
    }

    private fun buildMainBook(): JTabbedPane {

        return JTabbedPane(JTabbedPane.TOP).apply {

            addTab(TabCash.TITLE, TabCash() )

            addTab(TabReport.TITLE, TabReport() )
        }
    }

    private fun title(): String {
        val (userName, departmentName, workPlace, _, userId, _) = AfinaQuery.getUserDepartment()

        val user = userName ?: userId

        val db = if (AfinaQuery.isTestBaseConnect()) "TEST" else "AFINA"

        val header = "Сканер-Платежи"

        return "$header [$db] [$user] [$departmentName] [$workPlace]"
    }
}

private const val CHECK_WORKPLACE = "{ call od.XLS_REPORT_ALL.checkWorkplace }"