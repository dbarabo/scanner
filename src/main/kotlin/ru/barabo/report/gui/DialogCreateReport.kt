package ru.barabo.report.gui

import ru.barabo.gui.swing.*
import ru.barabo.report.entity.Directory
import ru.barabo.report.entity.Report
import ru.barabo.report.entity.StateReport
import ru.barabo.report.entity.defaultReportDirectory
import ru.barabo.report.service.DirectoryService
import ru.barabo.report.service.ReportService
import java.awt.Component
import java.awt.Desktop
import java.io.File
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JFileChooser
import javax.swing.JTextField
import javax.swing.filechooser.FileNameExtensionFilter

class DialogCreateReport(private val report: Report?, component: Component) : AbstractDialog(component, "Создание отчета") {

    private val nameReport: JTextField

    private val selectedXls: JButton

    private var selectedFile: File? = null

    private val directoryCombo: JComboBox<Directory>

    private val stateCombo: JComboBox<StateReport>

    init {
        title = report?.id?.let { "Правка отчета" } ?: "Создание отчета"

        comboBox("Папка-владелец", 0, DirectoryService.directoryList() ).apply {
            directoryCombo = this

            selectedItem = report?.directory?.let { DirectoryService.directoryById(it) }
        }

        textFieldHorizontal("Название отчета", 1).apply {
            nameReport = this

            text = report?.name
        }

        buttonHorisontal("xls-шаблон", "...", 2, ::selectXlsTemplateFile).apply {
            selectedXls = this

            text =  if(report?.fileName.isNullOrBlank() ) "..." else report?.fileName
        }

        comboBox("Состояние отчета", 3, StateReport.values().toList() ).apply {
            stateCombo = this

            selectedItem = report?.state?.let { StateReport.findByDbValue(it) }
        }

        groupPanel("Проверка xls-отчета", 4, width = 2).apply {
            onlyButton("Выгрузить", 0, 0, clickListener = ::downloadReportToRun).apply {
                isEnabled = report?.id != null
            }

            onlyButton("Загрузить обратно", 0, 1, clickListener = ::uploadReportToRun).apply {
                isEnabled = report?.id != null
            }
        }

        createOkCancelButton(6, 1)

        packWithLocation()
    }

    private fun downloadReportToRun() {

        processShowError {
            val template = report?.getTemplate( defaultReportDirectory() )

            Desktop.getDesktop().open(template)
        }
    }

    private fun uploadReportToRun() {

        processShowError {
            report?.uploadFile()

            ReportService.compileReport(report!!)

            showMessage("Отчет проверен успешно\nОшибки возможны только во время исполнения :)")
        }
    }

    override fun okProcess() {

        if(nameReport.text.isNullOrBlank()) throw Exception("Название отчета должно быть заполнено")

        if(directoryCombo.selectedItem == null) throw Exception("Папка-владелец должна быть указана")

        if(stateCombo.selectedItem == null) throw Exception("Состояние отчета должно быть выбрано")

        val directory = directoryCombo.selectedItem as Directory

        val state = stateCombo.selectedItem as StateReport

        if(report?.id == null) {
            if(selectedFile?.exists() != true) throw Exception("xls-файл шаблона должен быть заполнен")

            ReportService.createNewReport(nameReport.text, directory, state, selectedFile!!)
        } else {
            report.change(nameReport.text, directory, state, selectedFile)
            ReportService.updateReport(report)
        }
    }


    private fun selectXlsTemplateFile() {

        val fileChooser = JFileChooser().apply {
            isMultiSelectionEnabled = false

            fileFilter = FileNameExtensionFilter("файл шаблона (.xls)", "xls")

            fileSelectionMode = JFileChooser.FILES_ONLY
        }

        val selected = (if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            fileChooser.selectedFile else null) ?: return

        selectedFile = selected
        selectedXls.text = "...${selectedFile?.name}"
    }
}

