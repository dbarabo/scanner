package ru.barabo.report.gui

import ru.barabo.db.EditType
import ru.barabo.db.service.StoreListener
import ru.barabo.gui.swing.labelConstraint
import ru.barabo.gui.swing.onlyButton
import ru.barabo.gui.swing.processShowError
import ru.barabo.gui.swing.textConstraint
import ru.barabo.report.entity.Remark
import ru.barabo.report.service.RemarkService
import ru.barabo.report.service.ReportService
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Container
import java.awt.GridBagLayout
import javax.swing.*
import javax.swing.border.BevelBorder

class MessageInformer : JPanel(), StoreListener<List<Remark>> {

    private val infoArea = JTextArea().apply {
        isEditable = false
        wrapStyleWord = true
        lineWrap = true
        background = Color(245, 245, 245)
    }

    private val editor: JTextArea = JTextArea().apply {
        rows = 5
        border = BorderFactory.createBevelBorder(BevelBorder.LOWERED) //  BorderFactory.createLineBorder(Color.black)
        //wrapStyleWord = true
        //lineWrap = true
    }

    private val isPrivate: JCheckBox

    private val toolbar = JPanel().apply {
        layout = GridBagLayout()

        add(editor, textConstraint(0, 6, 0, 5) )

        add(JPanel().apply {

            layout = GridBagLayout()

            checkBox("Только для меня", 1, 0).apply {
                isPrivate = this
            }

            onlyButton("Добавить", 0, 0, "send24") {
                processShowError {
                    if(ReportService.selectedReport == null) throw Exception("Сначала выберите отчет")

                    if(ReportService.selectedReport?.isAccess != true)
                        throw Exception("Так как у Вас нет прав на этот отчет,\nто информацию по нему Вы добавлять не можете :(")

                    RemarkService.addInfo(editor.text?.trim(), isPrivate.isSelected)
                    editor.text = ""
                }
            }
        }, labelConstraint(0, 6))
    }

    init {
        layout = BorderLayout()

        add(JScrollPane(infoArea), BorderLayout.CENTER)

        add(toolbar, BorderLayout.SOUTH)

        RemarkService.addListener(this)
    }

    override fun refreshAll(elemRoot: List<Remark>, refreshType: EditType) {
        infoArea.text = RemarkService.fullInfo()
    }
}

fun Container.checkBox(title: String, gridY: Int, gridX: Int = 0, clickListener: ()->Unit = {}): JCheckBox {
    return JCheckBox(title).apply {

        addActionListener { clickListener() }

        this@checkBox.add(this, textConstraint(gridY, 1, gridX) )
    }
}