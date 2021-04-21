package org.jetbrains.research.ml.tasktracker.hint

import com.intellij.diff.DiffManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import org.jetbrains.research.ml.coding.assistant.dataset.model.DatasetTask
import org.jetbrains.research.ml.coding.assistant.dataset.model.MetaInfo
import org.jetbrains.research.ml.tasktracker.models.Task
import org.jetbrains.research.ml.tasktracker.tracking.TaskFileHandler
import org.jetbrains.research.ml.tasktracker.ui.panes.SurveyUiData
import org.jetbrains.research.ml.tasktracker.ui.window.showHintDiff
import java.io.File

object HintHandler {
    fun showHintDiff(task: Task, project: Project) {
        val psiFile = TaskFileHandler.getPsiFile(project, task)
        val metaInfo = SurveyUiData.createMetaInfoForTask(task)
        TaskFileHandler.setTempFileContent(project, psiFile.text)
        val tempPsiFile = TaskFileHandler.getTempPsiFile(project)
        TaskFileHandler.commitTempFile(project)
        val hintFile = CodingAssistantManager.getHintedFile(tempPsiFile, metaInfo)
        val hintText = hintFile?.text ?: TODO("log error")
        val diffManager = DiffManager.getInstance()
        diffManager.showHintDiff(
            project,
            psiFile.text,
            hintText,
            onApplyHandler = {
                TaskFileHandler.setFileContent(project, task, hintText)
                true
            }
        )
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
