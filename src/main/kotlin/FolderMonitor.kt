package org.example

import java.text.SimpleDateFormat
import java.util.*
import javax.imageio.ImageIO
import kotlin.concurrent.fixedRateTimer

class FolderMonitor(
    private val path: String
) {
    private val monitoredFiles: MutableMap<String, File> = mutableMapOf()
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
                }
            }
        }

        // Detect deleted files
        val deletedFiles = monitoredFiles.keys - currentFileNames
        for (deletedFile in deletedFiles) {
            monitoredFiles.remove(deletedFile)
        }

        // Update file statuses
        if (currentFiles != null && currentFiles.isNotEmpty()) {
            for (file in currentFiles) {
                val monitorFile = monitoredFiles[file.name]
                if (monitorFile != null && file.lastModified() != monitorFile.getLastChanged().toLong()) {
                    monitorFile.setLastChanged(file.lastModified())
                    monitorFile.setStatus("Changed")
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
                file.lastModified(),
                file.lastModified(),
                "No change",
                lineCount = file.readLines().size,
                wordCount = file.readText().split("\\s+".toRegex()).size,
                charactersCount = file.readText().length
            )

            AcceptedExtentions.kt, AcceptedExtentions.py -> return ProgramFile(
                file.name,
                extension,
                file.lastModified(),
                file.lastModified(),
                "No change",
                lineCount = file.readLines().size,
                classCount = file.readLines().count { it.contains("class ") },
                methodCount = file.readLines().count { it.contains("def ") || it.contains("fun ") }
            )

            AcceptedExtentions.jpg, AcceptedExtentions.png -> {
                val image = ImageIO.read(file)
                return ImageFile(
                    file.name,
                    extension,
                    file.lastModified(),
                    file.lastModified(),
                    "No change",
                    width = image.width,
                    height = image.height
                )
            }
        }
    }


    fun commit() {
        snapshot = System.currentTimeMillis()
        for (file in monitoredFiles.values) {
            file.setStatus("No change")
        }
        TODO("Update snapshot only when there are changes")
    }

    fun info(fileName: String) {
        if (fileName == "all files") {
            for (file in monitoredFiles.values) {
                println(file.getName() + " - " + file.getStatus())
                println("Extension: " + file.getExtension())
                println("Created: " + file.getCreated())
                println("Updated: " + file.getLastChanged())
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
    }
    fun startTimer() {
        fixedRateTimer("folder-monitor", initialDelay = 0, period = 5000) {
            refreshFiles()
        }
    }

}
