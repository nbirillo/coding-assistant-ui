package org.jetbrains.research.ml.codingAssistant.ui.window

import com.intellij.diff.*
import com.intellij.diff.chains.SimpleDiffRequestChain
import com.intellij.diff.requests.DiffRequest
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.diff.tools.simple.SimpleDiffTool
import com.intellij.diff.util.DiffUserDataKeysEx
import com.intellij.openapi.project.Project
import com.jetbrains.python.PythonFileType

fun DiffManager.showHintDiff(project: Project, original: String, hint: String) {
    val myDiffContentFactory = DiffContentFactoryEx.getInstanceEx()
    val originalContent = myDiffContentFactory.create(project, original, PythonFileType.INSTANCE)
    val hintContent = myDiffContentFactory.create(project, hint, PythonFileType.INSTANCE)
    val request = SimpleDiffRequest("Title", originalContent, hintContent, "Original", "Hint")
    val chain = SimpleDiffRequestChain(request)
    chain.putUserData(DiffUserDataKeysEx.FORCE_DIFF_TOOL, SimpleDiffTool.INSTANCE)
    HintDiffWindow(project, chain, DiffDialogHints.MODAL).show()
}

