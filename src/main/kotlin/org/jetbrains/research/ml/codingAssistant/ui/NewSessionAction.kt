package org.jetbrains.research.ml.codingAssistant.ui

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import org.jetbrains.research.ml.codingAssistant.server.PluginServer
import org.jetbrains.research.ml.codingAssistant.server.TrackerQueryExecutor
import org.jetbrains.research.ml.codingAssistant.tracking.StoredInfoWrapper
import org.jetbrains.research.ml.codingAssistant.tracking.TaskFileHandler
import org.jetbrains.research.ml.codingAssistant.ui.panes.SurveyControllerManager
import org.jetbrains.research.ml.codingAssistant.ui.panes.SurveyUiData
import org.jetbrains.research.ml.codingAssistant.ui.panes.TaskChoosingUiData
import org.jetbrains.research.ml.codingAssistant.ui.panes.util.changeVisiblePane

class NewSessionAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        /**
         * Todo:
         *  0. maybe send all data just in case?
         *  1. clear csv file
         */

        TaskFileHandler.deleteAllProjectFiles(e.project!!)
        StoredInfoWrapper.updateStoredInfo(surveyInfo = mapOf())
        TrackerQueryExecutor.initUserId()

        SurveyUiData.setDefaultValues()
        TaskChoosingUiData.setDefaultValues()
        changeVisiblePane(SurveyControllerManager)
        TaskFileHandler.addProject(e.project!!)
    }
}
