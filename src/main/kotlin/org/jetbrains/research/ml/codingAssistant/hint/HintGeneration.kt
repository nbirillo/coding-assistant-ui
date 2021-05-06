package org.jetbrains.research.ml.codingAssistant.hint

import com.intellij.diff.DiffManager
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.jetbrains.research.ml.coding.assistant.dataset.model.DatasetTask
import org.jetbrains.research.ml.coding.assistant.dataset.model.MetaInfo
import org.jetbrains.research.ml.codingAssistant.Plugin
import org.jetbrains.research.ml.codingAssistant.models.Task
import org.jetbrains.research.ml.codingAssistant.tracking.DocumentLogPrinter
import org.jetbrains.research.ml.codingAssistant.tracking.TaskFileHandler
import org.jetbrains.research.ml.codingAssistant.ui.panes.SurveyUiData
import org.jetbrains.research.ml.codingAssistant.ui.window.showHintDiff

object HintHandler {
    private val logger: Logger = Logger.getInstance(javaClass)

    fun showHintDiff(task: Task, project: Project) {
        val studentPsiFile = TaskFileHandler.getPsiFile(project, task)
        val studentCode = studentPsiFile.text
        val metaInfo = SurveyUiData.createMetaInfoForTask(task)
        logger.info("${Plugin.PLUGIN_NAME}: Requested hint for code:\n$studentCode")
        TaskFileHandler.setTempFileContent(project, studentPsiFile.text)
        val tempPsiFile = TaskFileHandler.getTempPsiFile(project)
        TaskFileHandler.commitTempFile(project)
        val codeHint = CodingAssistantManager.getHintedFile(tempPsiFile, metaInfo)
        if (codeHint == null) {
            logger.info("${Plugin.PLUGIN_NAME}: Cannot generate hint for code")
            showHintNotAvailableNotification(project)
            return
        }
        val hintText = codeHint.psiFragment.text
        if (hintText == studentCode && codeHint.vertexHint.hintVertex.isFinal) {
            logger.info("${Plugin.PLUGIN_NAME}: Cannot generate hint for correct solution")
            showSolutionIsCorrect(project)
            return
        }
        val diffManager = DiffManager.getInstance()
        diffManager.showHintDiff(
            project,
            studentCode,
            hintText,
            onApplyHandler = {
                logger.info("${Plugin.PLUGIN_NAME}: Generated hint is accepted by the student. New student's code:\n$hintText")
                TaskFileHandler.setFileContent(project, task, hintText)
                true
            },
            onCloseHandler = {
                logger.info("${Plugin.PLUGIN_NAME}: Generated hint is declined by the student. Old student's code:\n$studentCode")
                true
            }
        )
    }

    private fun showSolutionIsCorrect(project: Project) =
        NOTIFICATION_GROUP.createNotification(
            "Your code is correct. Try to submit your solution",
            NotificationType.INFORMATION
        )
            .notify(project)

    private fun showHintNotAvailableNotification(project: Project) {
        NOTIFICATION_GROUP.createNotification(
            "Hint is not available for this code",
            NotificationType.ERROR
        )
            .notify(project)
    }

    private val NOTIFICATION_GROUP =
        NotificationGroup("Custom Notification Group", NotificationDisplayType.BALLOON, true)
}

private fun SurveyUiData.createMetaInfoForTask(task: Task): MetaInfo {
    return MetaInfo(
        age.uiValue.toFloat(),
        MetaInfo.ProgramExperience.createFromYearsAndMonths(peYears.uiValue, peMonths.uiValue),
        0.0,
        DatasetTask.createFromString(task.key)
    )
}

private fun MetaInfo.ProgramExperience.Companion.createFromYearsAndMonths(
    years: Int,
    months: Int
): MetaInfo.ProgramExperience {
    val totalMonth = months + years * 12
    if (totalMonth < 0) {
        throw IllegalArgumentException()
    }
    return createFromMonths(totalMonth)
}
