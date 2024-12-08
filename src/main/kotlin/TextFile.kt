package org.example

class TextFile(
    private val name: String,
    private val extension: AcceptedExtentions,
    private var created: Long,
    private var lastChanged: Long,
    private var status: String,
    private var lineCount: Int,
    private var wordCount: Int,
    private var charactersCount: Int
) : File(name, extension, created, lastChanged, status) {
    fun getLineCount(): Int {
        return lineCount
    }
    fun getWordCount(): Int {
        return wordCount
    }
    fun getCharactersCount(): Int {
        return charactersCount
    }

}