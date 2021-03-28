package org.jetbrains.research.ml.codingAssistant.hint

import com.intellij.diff.DiffManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.psi.PsiDocumentManager
import org.jetbrains.research.ml.codingAssistant.ui.window.showHintDiff

class HintGenerationAction : AnAction("Show Hint") {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val document = FileEditorManager.getInstance(project).selectedTextEditor?.document ?: error("null document")
        val documentManager = project.service<PsiDocumentManager>()
        val otherText = """
a = int(input())
b = int(input())
c = int(input())
if (a > b, a > c):
    print(a)
elif (a == b, a > c):
    print(a, )
if (b > a, b > c):
    print(b)
elif (b > a, b == c):
    print(b, c)
if (c > a, c > b):
    print(c)
elif (c == a, c > b):
    print(a, c)
if (a == b, b == c):
    print(a, b, c)
        """.trimIndent()
        val currentPsiFile = documentManager.getPsiFile(document)

        print(currentPsiFile)
        val diffManager = DiffManager.getInstance()
        diffManager.showHintDiff(project, document.text, otherText)
    }
}
