package org.jetbrains.research.ml.codingAssistant.ui.window

import com.intellij.CommonBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.WindowWrapper
import com.intellij.openapi.util.BooleanGetter
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.Disposer
import com.intellij.util.ui.UIUtil
import java.awt.Component
import java.awt.Image
import java.awt.Window
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.border.Border

class HintDialogWindowWrapper(builder: HintWindowWrapperBuilder) : WindowWrapper {
    private val myProject: Project? = builder.project
    private val myComponent: JComponent = builder.myComponent
    private val myMode: WindowWrapper.Mode = builder.mode
    private val myDialog: MyDialogWrapper

    override fun dispose() = Disposer.dispose(myDialog.disposable)

    override fun show() = myDialog.show()

    override fun getProject(): Project? = myProject

    override fun getComponent(): JComponent = myComponent

    override fun getMode(): WindowWrapper.Mode = myMode

    override fun getWindow(): Window = myDialog.window

    override fun setTitle(title: String?) {
        myDialog.title = title ?: ""
    }

    override fun setImages(images: List<Image>?) = Unit

    override fun close() {
        myDialog.close(DialogWrapper.CANCEL_EXIT_CODE)
    }

    private class MyDialogWrapper : DialogWrapper {
        private val myComponent: JComponent

        private var myDimensionServiceKey: String? = null

        private var myPreferredFocusedComponentComputable: Computable<out JComponent>? = null

        private var myOnCloseHandler: BooleanGetter? = null

        private var myOnApplyHandler: BooleanGetter? = null

        constructor(project: Project?, component: JComponent) : super(project, true) {
            myComponent = component
        }

        constructor(parent: Component, component: JComponent) : super(parent, true) {
            myComponent = component
        }

        public override fun init() {
            super.init()
        }

        fun setParameters(
            dimensionServiceKey: String?,
            preferredFocusedComponent: Computable<out JComponent>?,
            onCloseHandler: BooleanGetter?,
            onApplyHandler: BooleanGetter?
        ) {
            myDimensionServiceKey = dimensionServiceKey
            myPreferredFocusedComponentComputable = preferredFocusedComponent
            myOnCloseHandler = onCloseHandler
            myOnApplyHandler = onApplyHandler
        }

        override fun createContentPaneBorder(): Border? = null

        override fun createCenterPanel(): JComponent = myComponent

        override fun getDimensionServiceKey(): String? = myDimensionServiceKey

        override fun getPreferredFocusedComponent(): JComponent? {
            return myPreferredFocusedComponentComputable?.compute() ?: super.getPreferredFocusedComponent()
        }

        override fun getOKAction(): Action {
            val okAction = super.getOKAction()
            okAction.putValue(Action.NAME, CommonBundle.getApplyButtonText())
            return okAction
        }

        override fun doCancelAction() {
            val handler = myOnCloseHandler
            if (handler != null && !handler.get()) return
            super.doCancelAction()
        }

        override fun doOKAction() {
            val handler = myOnApplyHandler
            if (handler != null && !handler.get()) return
            super.doOKAction()
        }
    }

    init {
        val parent = builder.parent
        myDialog = if (parent != null)
            MyDialogWrapper(parent, builder.myComponent)
        else
            MyDialogWrapper(builder.project, builder.myComponent)
        myDialog.setParameters(
            builder.dimensionServiceKey,
            builder.preferredFocusedComponent,
            builder.onCloseHandler,
            builder.onApplyHandler
        )
        // installOnShowCallback
        val onShowCallback = builder.onShowCallback
        if (onShowCallback != null) {
            UIUtil.runWhenWindowOpened(myDialog.window, onShowCallback)
        }
        setTitle(builder.title)
        when (builder.mode) {
            WindowWrapper.Mode.MODAL -> myDialog.isModal = true
            WindowWrapper.Mode.NON_MODAL -> myDialog.isModal = false
            else -> assert(false)
        }
        myDialog.init()
        Disposer.register(myDialog.disposable, this)
    }
}
