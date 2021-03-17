package org.jetbrains.research.ml.codingAssistant.tracking

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.WritingAccessProvider
import org.jetbrains.research.ml.codingAssistant.models.Task
import org.jetbrains.research.ml.codingAssistant.tracking.TaskFileHandler.isItsFileWritable
import org.jetbrains.research.ml.codingAssistant.tracking.dialog.ReadOnlyDialogWrapper

class PluginAccessProvider : WritingAccessProvider() {
    override fun requestWriting(files: MutableCollection<out VirtualFile>): MutableCollection<VirtualFile> {
//        Tasks which files requested writing but are read-only because of not chosen task
        val tasksOfReadOnlyFiles = arrayListOf<Task>()
        val (_, readOnlyFiles) = files.partition {
            val task = TaskFileHandler.getTaskByVirtualFile(it)
            if (task.isItsFileWritable()) {
                true
            } else {
                task?.let { tasksOfReadOnlyFiles.add(task) }
                false
            }
        }

        if (tasksOfReadOnlyFiles.isNotEmpty()) {
            ApplicationManager.getApplication().invokeAndWait {
                ReadOnlyDialogWrapper(
                    tasksOfReadOnlyFiles.first()
                ).show()
            }
        }

        return readOnlyFiles.toMutableList()
    }
}
