package org.jetbrains.research.ml.tasktracker.hint

import com.intellij.diff.DiffManager
import com.intellij.openapi.project.Project
import org.jetbrains.research.ml.coding.assistant.dataset.model.DatasetTask
import org.jetbrains.research.ml.coding.assistant.dataset.model.MetaInfo
import org.jetbrains.research.ml.tasktracker.models.Task
import org.jetbrains.research.ml.tasktracker.ui.window.showHintDiff
import org.jetbrains.research.ml.tasktracker.tracking.TaskFileHandler
import org.jetbrains.research.ml.tasktracker.ui.panes.SurveyUiData

object HintHandler {
    fun showHintDiff(task: Task, project: Project) {
        val psiFile = TaskFileHandler.getPsiFile(project, task)
        val metaInfo = SurveyUiData.createMetaInfoForTask(task)
        val hint = CodingAssistantManager.getHintedFile(psiFile, metaInfo) ?: TODO("log error")
        val hintText = hint.text
        val diffManager = DiffManager.getInstance()
        diffManager.showHintDiff(project, psiFile.text, hintText, onApplyHandler = {
            TaskFileHandler.setFileContent(project, task, hintText)
            true
        })
    }
}

private fun SurveyUiData.createMetaInfoForTask(task: Task): MetaInfo {
    return MetaInfo(
        age.uiValue.toFloat(),
        MetaInfo.ProgramExperience.createFromYearsAndMonths(peYears.uiValue, peMonths.uiValue),
        null,
        DatasetTask.createFromString(task.key)
    )
}

private fun MetaInfo.ProgramExperience.Companion.createFromYearsAndMonths(
    years: Int,
    months: Int
): MetaInfo.ProgramExperience? {
    val totalMonth = months + years * 12
    if (totalMonth < 0)
        return null
    return createFromMonths(totalMonth)
}
