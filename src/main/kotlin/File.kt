package org.example

import java.text.SimpleDateFormat
import java.util.*

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

    fun getCreated(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(created))
    }

    fun getLastChanged(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(lastChanged))
    }

    fun getStatus(): String {
        return status
    }
    fun setLastChanged(time: Long) {
        lastChanged = time
    }
}
