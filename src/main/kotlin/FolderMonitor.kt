package org.example

import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.util.*
import javax.imageio.ImageIO
import kotlin.concurrent.fixedRateTimer

class FolderMonitor(
    private val path: String
) {
    private val monitoredFiles: MutableMap<String, File> = mutableMapOf()
    private val deletedDicOfFiles: MutableMap<String, File> = mutableMapOf()
    private var snapshot: Long = System.currentTimeMillis()

    fun refreshFiles() {
        val directory = java.io.File(path)
        if (!directory.exists() || !directory.isDirectory) {
            throw Error("Invalid folder path!")
        }

        val currentFiles = directory.listFiles()
        val currentFileNames = mutableListOf<String>()
        if (!currentFiles.isNullOrEmpty()) {
            for (file in currentFiles) {
                if (file.isFile) {
                    currentFileNames.add(file.name)
                }
            }
        }

        // Detect new files
        if (!currentFiles.isNullOrEmpty()) {
            for (file in currentFiles) {
                if (file.isFile && !monitoredFiles.containsKey(file.name)) {
                    val monitorFile = createFileMonitor(file)
                    monitoredFiles[file.name] = monitorFile
                    println("\n${monitorFile.getName()} was CREATED.\nsrc/trackedRepository> ")
                }
            }
        }

        // Detect deleted files
        val deletedFiles = monitoredFiles.keys - currentFileNames
        for (deletedFile in deletedFiles) {
            val monitorFile = monitoredFiles[deletedFile]
            if (monitorFile != null) {
                monitorFile.setStatus("Deleted")
                deletedDicOfFiles[deletedFile] = monitorFile
                monitoredFiles.remove(deletedFile)
                println("\n${monitorFile.getName()} was DELETED.\nsrc/trackedRepository> ")
            }
        }

        // Update file statuses
        if (currentFiles != null) {
            for (file in currentFiles) {
                val monitorFile = monitoredFiles[file.name]
                if (monitorFile != null && file.lastModified() != monitorFile.getLastChanged().toLong()) {
                    monitorFile.setLastChanged(file.lastModified())
                    monitorFile.setStatus("Changed")
                    updateFileAttributes(monitorFile, file)
                    println("\n${monitorFile.getName()} was UPDATED.\nsrc/trackedRepository> ")

                }
            }
        }

    }

    private fun createFileMonitor(file: java.io.File): File {
        val extension = AcceptedExtentions.entries.find { file.name.endsWith(it.name) }
        if (extension == null) {
            throw Error("Unsupported file extension: ${file.extension}")
        }
        when (extension) {
            AcceptedExtentions.txt -> return TextFile(
                file.name,
                extension,
                Files.readAttributes(file.toPath(), BasicFileAttributes::class.java).creationTime().toMillis(),
                file.lastModified(),
                "New File",
                lineCount = file.readLines().size,
                wordCount = file.readText().split("\\s+".toRegex()).filter { it.isNotEmpty() }.size,
                charactersCount = file.readText().length
            )

            AcceptedExtentions.kt, AcceptedExtentions.py -> return ProgramFile(
                file.name,
                extension,
                Files.readAttributes(file.toPath(), BasicFileAttributes::class.java).creationTime().toMillis(),
                file.lastModified(),
                "New File",
                lineCount = file.readLines().size,
                classCount = file.readLines().count { it.contains("class ") },
                methodCount = file.readLines().count { it.contains("def ") || it.contains("fun ") }
            )

            AcceptedExtentions.jpg, AcceptedExtentions.png -> {
                val image = ImageIO.read(file)
                return ImageFile(
                    file.name,
                    extension,
                    Files.readAttributes(file.toPath(), BasicFileAttributes::class.java).creationTime().toMillis(),
                    file.lastModified(),
                    "New File",
                    width = image.width,
                    height = image.height
                )
            }
        }
    }


    fun commit() {
        val hasChanges = monitoredFiles.values.any { it.getStatus() != "No change" }
        if (hasChanges) {
            for (file in monitoredFiles.values) {
                file.setStatus("No change")
            }
            snapshot = System.currentTimeMillis()
        } else {
            println("No changes to commit.")
        }

    }

    fun info(fileName: String) {
        if (fileName == "all") {
            for (file in monitoredFiles.values) {
                println(file.getName() + " - " + file.getStatus())
                println("Extension: " + file.getExtension())
                println("Created: " + SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(file.getCreated())))
                println("Updated: " + SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(file.getLastChanged())))
            }
            for (file in deletedDicOfFiles.values) {
                println(file.getName() + " - " + file.getStatus())
                println("Extension: " + file.getExtension())
                println("Created: " + SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(file.getCreated())))
                println("Updated: " + SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(file.getLastChanged())))
            }

        } else {
            var file: File? = null
            for (existingFile in monitoredFiles.values) {
                if (existingFile.getName() == fileName) {
                    file = existingFile
                    break
                }
            }
            if (file == null) {
                throw Error("File not found")
            } else {
                when (file) {
                    is ImageFile -> println("Dimensions: ${file.getWidth()} x ${file.getHeight()}")
                    is TextFile -> {
                        println("Line Count: ${file.getLineCount()}")
                        println("Word Count: ${file.getWordCount()}")
                        println("Character Count: ${file.getCharactersCount()}")
                    }

                    is ProgramFile -> {
                        println("Line Count: ${file.getLineCount()}")
                        println("Class Count: ${file.getClassCount()}")
                        println("Method Count: ${file.getMethodCount()}")
                    }
                }
            }
        }
    }

    fun status() {
        println("Created Snapshot at: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(snapshot))}")
        for (file in monitoredFiles.values) {
            println(file.getName() + " - " + file.getStatus())
        }

        for (file in deletedDicOfFiles.values) {
            println(file.getName() + " - " + file.getStatus())
        }

    }

    fun startTimer() {
        fixedRateTimer("folder-monitor", initialDelay = 0, period = 5000) {
            refreshFiles()
        }
    }

    private fun updateFileAttributes(fileToUpdate: File, file: java.io.File) {
        when (fileToUpdate) {
            is TextFile -> {
                fileToUpdate.setLineCount(file.readLines().size)
                fileToUpdate.setWordCount(file.readText().split("\\s+".toRegex()).filter { it.isNotEmpty() }.size)
                fileToUpdate.setCharactersCount(file.readText().length)
            }

            is ProgramFile -> {
                fileToUpdate.setLineCount(file.readLines().size)
                fileToUpdate.setClassCount(file.readLines().count { it.contains("class ") })
                fileToUpdate.setMethodCount(file.readLines().count { it.contains("def ") || it.contains("fun ") })
            }

            is ImageFile -> {
                val image = ImageIO.read(file)
                fileToUpdate.setWidth(image.width)
                fileToUpdate.setHeight(image.height)
            }
        }
    }

}
