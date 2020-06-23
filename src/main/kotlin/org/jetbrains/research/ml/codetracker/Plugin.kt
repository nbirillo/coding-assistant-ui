package org.jetbrains.research.ml.codetracker

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
import org.jetbrains.research.ml.codetracker.server.TrackerQueryExecutor


object Plugin {
    const val PLUGIN_ID = "codetracker"

    private val logger: Logger = Logger.getInstance(javaClass)

    init {
        logger.info("$PLUGIN_ID: init plugin")
    }

    fun stopTracking(): Boolean {
        logger.info("$PLUGIN_ID: close IDE")
        logger.info("$PLUGIN_ID: prepare fo sending ${DocumentLogger.getFiles().size} files")
        if (DocumentLogger.getFiles().isNotEmpty()) {
            DocumentLogger.logCurrentDocuments()
            DocumentLogger.flush()
            DocumentLogger.documentsToPrinters.forEach { (d, p) ->
                TrackerQueryExecutor.sendCodeTrackerData(
                    p.file,
                    { TrackerQueryExecutor.isLastSuccessful }
                ) { DocumentLogger.close(d, p) }
            }
        }
        FileHandler.stopTracking()
        return TrackerQueryExecutor.isLastSuccessful
    }

}
