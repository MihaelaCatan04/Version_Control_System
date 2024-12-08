import org.example.FolderMonitor

fun main() {
    val folderMonitor = FolderMonitor("src/trackedRepository")
    folderMonitor.refreshFiles()
    folderMonitor.startTimer()

    while (true) {
        print("src/trackedRepository> ")
        val command = readLine()!!.split(" ")
        when (command[0]) {
            "commit" -> folderMonitor.commit()
            "status" -> folderMonitor.status()
            "info" -> if (command.size > 1) folderMonitor.info(command[1]) else println("File name required!")
            else -> println("Unknown command!")
        }
    }
}