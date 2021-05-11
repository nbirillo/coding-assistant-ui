package org.jetbrains.research.ml.codingAssistant.ui

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.jetbrains.research.ml.codingAssistant.tracking.StoredInfoHandler
import org.jetbrains.research.ml.codingAssistant.tracking.TaskFileHandler

class NewSessionAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        /**
         * Todo:
         *  0. maybe send all data just in case?
         *  1. delete profile info
         *  3. + delete task files
         *  2. send new request for id
         *  4. set profile panel active
         */

//      Maybe we need to delete files for all open projects?
        TaskFileHandler.deleteAllProjectFiles(e.project!!)

        println("new action")
    }

}
