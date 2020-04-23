package ru.barabo.report.service

import ru.barabo.afina.AfinaOrm
import ru.barabo.afina.AfinaQuery
import ru.barabo.db.annotation.ParamsSelect
import ru.barabo.db.service.StoreFilterService
import ru.barabo.report.entity.Directory
import ru.barabo.report.entity.GroupDirectory
import ru.barabo.report.entity.NULL_DIRECTORY
import java.lang.Exception

object DirectoryService : StoreFilterService<Directory>(AfinaOrm, Directory::class.java), ParamsSelect {

    lateinit var directories: MutableList<GroupDirectory>
    private set

    private var parentGroup: GroupDirectory? = null

    override fun selectParams(): Array<Any?>? = arrayOf(AfinaQuery.getUserDepartment().workPlaceId)

    fun directoryList() = dataList.toList()

    var selectedDirectory: GroupDirectory? = null

    override fun initData() {

        if(!::directories.isInitialized) {
            directories = ArrayList()
        }

        directories.clear()

        parentGroup = null

        super.initData()

        selectedDirectory = if(directories.isEmpty()) null else directories[0]
    }

    override fun processInsert(item: Directory) {

        val parent = item.parent?.let { parentGroup }

        val reports = ReportService.reportsByDirectory(item.id)

        val group = GroupDirectory(directory = item, parent = parent, reports = reports)

        if(parent == null) {
            parentGroup = group
            directories.add(group)
        } else {
            parentGroup?.childDirectories?.add(group)
        }
    }

    fun directoryById(directoryId: Long?): Directory? =
        if(directoryId == null) null else dataList.firstOrNull { it.id == directoryId }

    fun parentDirectories(): List<Directory> {
        val directories = ArrayList<Directory>()

        directories += NULL_DIRECTORY

        val dirs = dataList.filter { it.parent == null }

        directories.addAll(dirs)

        return directories
    }

    fun createDirectory(name: String?, parent: Directory?) {
        if(name.isNullOrBlank()) throw Exception("Название папки не может быть пустым")

        val directory = Directory(parent = parent?.id, name = name)

        parentGroup = parent?.id?.let { findGroupByDirectoryId(it) }

        val newDirectory = save(directory)

        selectedDirectory = findGroupByDirectoryId(newDirectory.id!!)
    }

    fun updateDirectory(directory: Directory, name: String?, parent: Directory?) {
        if(name.isNullOrBlank()) throw Exception("Название папки не может быть пустым")

        directory.name = name
        directory.parent = parent?.id
        parentGroup = null
        save(directory)
        initData()
        selectedDirectory = findGroupByDirectoryId(directory.id!!)
    }

    fun findGroupByDirectoryId(id: Long): GroupDirectory?  = directories.firstOrNull { it.directory.id == id }
}