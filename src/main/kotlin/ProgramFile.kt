package org.example

// Class for Program file
class ProgramFile(
    private val name: String,
    private val extension: AcceptedExtentions,
    private var created: Long,
    private var lastChanged: Long,
    private var status: String,
    private var lineCount: Int,
    private var classCount: Int,
    private var methodCount: Int
) : File(name, extension, created, lastChanged, status) {
    fun getLineCount(): Int {
        return lineCount
    }

    fun getClassCount(): Int {
        return classCount
    }

    fun getMethodCount(): Int {
        return methodCount
    }

    fun setLineCount(lineCount: Int) {
        this.lineCount = lineCount
    }

    fun setClassCount(classCount: Int) {
        this.classCount = classCount
    }

    fun setMethodCount(methodCount: Int) {
        this.methodCount = methodCount
    }
}