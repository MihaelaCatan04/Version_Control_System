package org.example

import java.text.SimpleDateFormat
import java.util.*

// Abstract class for file
abstract class File(
    private val name: String,
    private val extension: AcceptedExtentions,
    private var created: Long,
    private var lastChanged: Long,
    private var status: String
) {
    fun setStatus(newStatus: String) {
        status = newStatus
    }

    fun getName(): String {
        return name
    }

    fun getExtension(): AcceptedExtentions {
        return extension
    }

    fun getCreated(): Long {
        return created
    }

    fun getLastChanged(): Long {
        return lastChanged
    }

    fun getStatus(): String {
        return status
    }

    fun setLastChanged(time: Long) {
        lastChanged = time
    }
}
