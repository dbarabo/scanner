package ru.barabo.report.gui

import ru.barabo.gui.swing.AbstractDialog
import ru.barabo.gui.swing.comboBox
import ru.barabo.gui.swing.textFieldHorizontal
import ru.barabo.report.entity.Directory
import ru.barabo.report.entity.NULL_DIRECTORY
import ru.barabo.report.service.DirectoryService
import java.awt.Component
import javax.swing.JComboBox
import javax.swing.JTextField

class DialogCreateDirectory(private val directory: Directory?, component: Component) : AbstractDialog(component, "Создание папки") {

    private val comboParent: JComboBox<Directory>

    private val nameField: JTextField

    init {
        title = directory?.id?.let { "Правка папки" } ?: "Создание папки"

        comboBox("Родительская папка", 0, DirectoryService.parentDirectories() ).apply {

            selectedItem =  directory?.parent?.let { DirectoryService.directoryById(it) } ?: NULL_DIRECTORY

            comboParent = this
        }

        textFieldHorizontal("Название папки", 1).apply {
            text = directory?.name

            nameField = this
        }

        createOkCancelButton(2, 1)

        packWithLocation()
    }

    override fun okProcess() {
        if(directory?.id == null) {
            DirectoryService.createDirectory(nameField.text, comboParent.selectedItem as? Directory)
        } else {
            DirectoryService.updateDirectory(directory, nameField.text, comboParent.selectedItem as? Directory)
        }
    }
}