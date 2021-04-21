package org.jetbrains.research.ml.tasktracker.hint

import com.intellij.diff.DiffManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.DumbService
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
        val copyPsiFile = createDummyPsiFile(project, psiFile.text)
        val hintFile = project.service<DumbService>().runReadActionInSmartMode<PsiFile?> {
            CodingAssistantManager.getHintedFile(copyPsiFile, metaInfo)
        }
        val hintText = hintFile?.text ?: TODO("log error")
        val diffManager = DiffManager.getInstance()
        deleteDummyFile(project)
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

    private fun commitDummyDocument(project: Project) {
        val file = File("${project.basePath}/.tmp.py")
        val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file) ?: return
        val document = FileDocumentManager.getInstance().getDocument(virtualFile) ?: return
        project.service<PsiDocumentManager>().commitDocument(document)
    }

    private fun createDummyPsiFile(project: Project, content: String): PsiFile {
        val document = createDummyDocument(project)
        WriteCommandAction.runWriteCommandAction(project) {
            document.setText(content)
        }
        commitDummyDocument(project)
        return PsiDocumentManager.getInstance(project).getPsiFile(document)!!
    }

    private fun deleteDummyFile(project: Project) {
        val file = File("${project.basePath}/.tmp.py")
        val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)!!
        LocalFileSystem.getInstance().deleteFile(this, virtualFile)
        LocalFileSystem.getInstance().refresh(true)
    }

    private fun createDummyDocument(project: Project): Document {
        val file = File("${project.basePath}/.tmp.py")
        FileUtil.createIfDoesntExist(file)
        val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)!!
        return FileDocumentManager.getInstance().getDocument(virtualFile)!!
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
