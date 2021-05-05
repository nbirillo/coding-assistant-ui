package org.jetbrains.research.ml.codingAssistant.ui.window

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.WindowWrapper
import com.intellij.openapi.util.BooleanGetter
import com.intellij.openapi.util.Computable
import java.awt.Component
import javax.swing.JComponent

class HintWindowWrapperBuilder(val mode: WindowWrapper.Mode, val myComponent: JComponent) {
    var project: Project? = null
    var parent: Component? = null
    var title: String? = null
    var preferredFocusedComponent: Computable<JComponent>? = null
    var dimensionServiceKey: String? = null
    var onShowCallback: Runnable? = null
    var onApplyHandler: BooleanGetter? = null
    var onCloseHandler: BooleanGetter? = null

    fun build(): WindowWrapper {
        return when (mode) {
            WindowWrapper.Mode.MODAL, WindowWrapper.Mode.NON_MODAL -> HintDialogWindowWrapper(this)
            WindowWrapper.Mode.FRAME -> error("Not supported for now")
        }
    }
}
