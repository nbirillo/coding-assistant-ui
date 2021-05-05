package org.jetbrains.research.ml.codingAssistant.tracking

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.io.FileUtil
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.jetbrains.research.ml.codingAssistant.Plugin
import org.jetbrains.research.ml.codingAssistant.Plugin.codingAssistantFolderPath
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

/**
 * Prints logs to the [logFile] in .csv format using [csvPrinter]
 */
data class LogPrinter(val csvPrinter: CSVPrinter, val fileWriter: OutputStreamWriter, val logFile: File) {
    companion object {
        private const val MAX_DIF_SIZE = 300
        private const val MAX_FILE_SIZE = 50 * 1024 * 1024
    }

    fun isFull(): Boolean {
        return logFile.length() > MAX_FILE_SIZE - MAX_DIF_SIZE
    }
}

/**
 * Takes care of all [logPrinters] (there may be several in case of log file overflowing) created to log a tracked document
 */
class DocumentLogPrinter {
    var logPrinters = mutableListOf<LogPrinter>()
        private set

    companion object {
        private val logger: Logger = Logger.getInstance(this::class.java)
    }

    /**
     * Gets the active logPrinter or creates a new one if there was none or the active one was full
     */
    fun getActiveLogPrinter(document: Document) : LogPrinter {
        val activePrinter = getLastPrinter(document)
        return if (activePrinter.isFull()) {
            addLogPrinter(document)
        } else {
            activePrinter
        }
    }

    private fun getLastPrinter(document: Document) : LogPrinter {
        return if (logPrinters.size == 0) {
            addLogPrinter(document)
        } else {
            logPrinters.last()
        }
    }

    private fun addLogPrinter(document: Document) : LogPrinter {
        logger.info("${Plugin.PLUGIN_NAME}: init printer")
        val logFile = createLogFile(document)
        val fileWriter = OutputStreamWriter(FileOutputStream(logFile), StandardCharsets.UTF_8)
        val csvPrinter = CSVPrinter(fileWriter, CSVFormat.DEFAULT)
        csvPrinter.printRecord(DocumentLoggedData.headers + UiLoggedData.headers)
        logPrinters.add(LogPrinter(csvPrinter, fileWriter, logFile))
        return logPrinters.last()
    }

    private fun createLogFile(document: Document) : File {
        File(codingAssistantFolderPath).mkdirs()
        val trackedFile = FileDocumentManager.getInstance().getFile(document)
        logger.info("${Plugin.PLUGIN_NAME}: create log file for tracked file ${trackedFile?.name}")
        val logFilesNumber = logPrinters.size
        val logFile = File("$codingAssistantFolderPath/${trackedFile?.nameWithoutExtension}_${trackedFile.hashCode()}_${document.hashCode()}_$logFilesNumber.csv")
        FileUtil.createIfDoesntExist(logFile)
        return logFile
    }

    /**
     * Keep only the last active printer
     */
    fun removeInactivePrinters() {
        if (logPrinters.size > 1) {
            logPrinters.dropLast(1).forEach { it.csvPrinter.close() }
            logPrinters = mutableListOf(logPrinters.last())
        }
    }

    /**
     * We need to flush printers before getting their log files.
     */
    fun getLogFiles() : List<File> {
        return logPrinters.map {
            it.csvPrinter.flush()
            it.logFile
        }
    }
}

object DocumentLogger {
    private val myDocumentsToPrinters: HashMap<Document, DocumentLogPrinter> = HashMap()

    // Call it after GetHint button pressing
    fun log(document: Document) {
        val docPrinter = myDocumentsToPrinters.getOrPut(document, { DocumentLogPrinter() })
        val logPrinter = docPrinter.getActiveLogPrinter(document)
        // TODO: log a hint, accept/reject, the user code after applying/rejecting hint, the final solution
        logPrinter.csvPrinter.printRecord(DocumentLoggedData.getData(document) + UiLoggedData.getData(Unit))
    }

    fun getDocumentLogPrinter(document: Document): DocumentLogPrinter? {
        return myDocumentsToPrinters[document]
    }
}
