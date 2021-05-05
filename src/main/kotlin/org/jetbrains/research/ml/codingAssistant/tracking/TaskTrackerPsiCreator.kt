package org.jetbrains.research.ml.codingAssistant.tracking

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiCreator
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiFileWrapper

class TaskTrackerPsiCreator(private val project: Project) : PsiCreator {
    inner class PsiFileWrapperImpl private constructor(private val psi: PsiFile) : PsiFile by psi, PsiFileWrapper {
        constructor(code: String) : this(updateTempFile(code))

        override fun deleteFile() = Unit

        override fun forceDeleteTmpData(): Boolean = false
    }

    private fun updateTempFile(code: String): PsiFile {
        TaskFileHandler.setTempFileContent(project, code)
        return TaskFileHandler.getTempPsiFile(project)
    }

    override fun initFileToPsi(code: String): PsiFileWrapper = PsiFileWrapperImpl(code)
}
