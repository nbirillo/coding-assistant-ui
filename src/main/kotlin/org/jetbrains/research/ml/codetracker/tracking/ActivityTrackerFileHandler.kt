package org.jetbrains.research.ml.codetracker.tracking

import krangl.*
import org.apache.commons.csv.CSVFormat
import org.jetbrains.research.ml.codetracker.Plugin.PLUGIN_ID
import org.jetbrains.research.ml.codetracker.models.Language
import org.jetbrains.research.ml.codetracker.server.PluginServer
import java.io.File

enum class ActivityTrackerColumn {
    TIMESTAMP, USERNAME, EVENT_TYPE, EVENT_DATA, PROJECT_NAME, FOCUSED_COMPONENT,
    CURRENT_FILE, PSI_PATH, EDITOR_LINE, EDITOR_COLUMN
}

object ActivityTrackerFileHandler {

    // TODO: get the current language instead of the argument??
    fun filterActivityTrackerData(filePath: String, language: Language = Language.PYTHON) {
        val df = DataFrame.readCSV(filePath,
            format=CSVFormat.DEFAULT.withHeader(ActivityTrackerColumn::class.java))
        val filteredDf = filterDataFrame(df, language)
        filteredDf.writeCSV(File(filePath), format=CSVFormat.DEFAULT.withIgnoreHeaderCase(true))
    }

    private fun filterDataFrame(df: DataFrame, language: Language): DataFrame {
        // Remove columns, which can contain the private information
        val anonymousDf = df.remove(ActivityTrackerColumn.USERNAME.name, ActivityTrackerColumn.PROJECT_NAME.name,
                                    ActivityTrackerColumn.PSI_PATH.name)
        // Keep only tasks files
        return removeUserFiles(anonymousDf, language)
    }

    private fun removeUserFiles(df: DataFrame, language: Language): DataFrame {
        // Get tasks for the regular expression in the following format: (task_1.key|task_2.key)
        val tasks = PluginServer.tasks.joinToString(separator = "|") { it.key }
        val matchCondition = ".*/$PLUGIN_ID/($tasks)${language.extension.ext}".toRegex(
            RegexOption.IGNORE_CASE
        )
        return df.filterByRow { (it[ActivityTrackerColumn.CURRENT_FILE.name] as String).matches(matchCondition)}
    }
}