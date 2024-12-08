package org.example

class ImageFile(
    private val name: String,
    private val extension: AcceptedExtentions,
    private var created: Long,
    private var lastChanged: Long,
    private var status: String,
    private var width: Int,
    private var height: Int
) : File(name, extension, created, lastChanged, status) {
    fun getWidth(): Int {
        return width
    }

    fun getHeight(): Int {
        return height
    }
}

