package org.jetbrains.research.ml.codingAssistant.ui.window

import com.intellij.diff.DiffDialogHints
import com.intellij.diff.chains.DiffRequestChain
import com.intellij.diff.impl.DiffRequestProcessor
import com.intellij.diff.impl.DiffWindow
import com.intellij.diff.util.DiffUserDataKeys
import com.intellij.diff.util.DiffUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.WindowWrapper
import com.intellij.openapi.util.BooleanGetter
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.Disposer
import com.intellij.util.ui.UIUtil
import java.awt.*
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.border.Border
import kotlin.math.max

class HintDiffWindow(
    project: Project,
    chain: DiffRequestChain,
    hints: DiffDialogHints
) : DiffWindow(project, chain, hints) {
    private var myWindowWrapper: WindowWrapper? = null
    private var myProcessor: DiffRequestProcessor? = null

    override fun init() {
        if (myWindowWrapper != null)
            return

        val dialogGroupKey = myProcessor?.getContextUserData(DiffUserDataKeys.DIALOG_GROUP_KEY) ?: "DiffContextDialog"

        val processor = createProcessor()
        val builder = HintWindowWrapperBuilder(DiffUtil.getWindowMode(myHints), MyPanel(processor.component)).apply {
            project = myProject
            parent = myHints.parent
            dimensionServiceKey = dialogGroupKey
            preferredFocusedComponent = Computable { myProcessor?.preferredFocusedComponent }
            onShowCallback = Runnable { myProcessor?.updateRequest() }
            onCloseHandler = BooleanGetter {
                true
            }
            onApplyHandler = BooleanGetter {
                true
            }
        }
        val windowWrapper = builder.build()
        windowWrapper.setImages(DiffUtil.DIFF_FRAME_ICONS.value)
        Disposer.register(windowWrapper, processor)

        myHints.windowConsumer?.consume(windowWrapper)

        myProcessor = processor
        myWindowWrapper = windowWrapper
    }

    override fun show() {
        init()
        myWindowWrapper?.show()
    }

    override fun getWrapper(): WindowWrapper? = myWindowWrapper

    private class MyPanel(content: JComponent) : JPanel(BorderLayout()) {
        init {
            add(content, BorderLayout.CENTER)
        }

        override fun getPreferredSize(): Dimension {
            val windowSize = DiffUtil.getDefaultDiffWindowSize()
            val size = super.getPreferredSize()
            return Dimension(max(windowSize.width, size.width), max(windowSize.height, size.height))
        }
    }
}
