package org.jetbrains.research.ml.codingAssistant.hint

import com.intellij.diff.DiffManager
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import org.jetbrains.research.ml.coding.assistant.dataset.model.DatasetTask
import org.jetbrains.research.ml.coding.assistant.dataset.model.MetaInfo
import org.jetbrains.research.ml.codingAssistant.models.Task
import org.jetbrains.research.ml.codingAssistant.tracking.DocumentLogger
import org.jetbrains.research.ml.codingAssistant.tracking.HintLoggedData
import org.jetbrains.research.ml.codingAssistant.tracking.TaskFileHandler
import org.jetbrains.research.ml.codingAssistant.ui.panes.SurveyUiData
import org.jetbrains.research.ml.codingAssistant.ui.window.showHintDiff

object HintHandler {
    fun showHintDiff(task: Task, project: Project) {
        val studentPsiFile = TaskFileHandler.getPsiFile(project, task)
        val beforeHintStudentCode = studentPsiFile.text
        val metaInfo = SurveyUiData.createMetaInfoForTask(task)
        TaskFileHandler.setTempFileContent(project, studentPsiFile.text)
        val tempPsiFile = TaskFileHandler.getTempPsiFile(project)
        TaskFileHandler.commitTempFile(project)
        val codeHint = CodingAssistantManager.getHintedFile(tempPsiFile, metaInfo)
        if (codeHint == null) {
            showHintNotAvailableNotification(project)
            logHintAction(
                task,
                project,
                null,
                HintLoggedData.HintStatus.NOT_FOUND,
                beforeHintStudentCode,
                null
            )
            return
        }
        val afterHintStudentCode = codeHint.psiFragment.text
        if (afterHintStudentCode == beforeHintStudentCode && codeHint.vertexHint.hintVertex.isFinal) {
            logHintAction(
                task,
                project,
                afterHintStudentCode,
                HintLoggedData.HintStatus.ON_CORRECT,
                beforeHintStudentCode,
                afterHintStudentCode
            )
            showSolutionIsCorrect(project)
            return
        }
        val diffManager = DiffManager.getInstance()
        diffManager.showHintDiff(
            project,
            beforeHintStudentCode,
            afterHintStudentCode,
            onApplyHandler = {
                WriteCommandAction.runWriteCommandAction(project) {
                    TaskFileHandler.setFileContent(project, task, afterHintStudentCode)
                }
                logHintAction(
                    task,
                    project,
                    afterHintStudentCode,
                    HintLoggedData.HintStatus.ACCEPTED,
                    beforeHintStudentCode,
                    afterHintStudentCode
                )
                true
            },
            onCloseHandler = {
                logHintAction(
                    task,
                    project,
                    afterHintStudentCode,
                    HintLoggedData.HintStatus.REJECTED,
                    beforeHintStudentCode,
                    afterHintStudentCode
                )
                true
            }
        )
    }

    private fun logHintAction(
        task: Task,
        project: Project,
        hint: String?,
        hintStatus: HintLoggedData.HintStatus,
        beforeHintUserCore: String,
        afterHintUserCode: String?
    ) {
        val document = TaskFileHandler.getDocument(project, task)
        DocumentLogger.logHintAction(document, hint, hintStatus, beforeHintUserCore, afterHintUserCode)
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
