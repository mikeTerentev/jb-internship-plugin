package ru.itmo.rain.terentev.finder

import com.intellij.notification.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import java.nio.file.Files
import java.nio.file.Paths
import javax.swing.event.HyperlinkEvent
import com.intellij.notification.NotificationType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.vfs.LocalFileSystem


class ProjectTypeFinder : StartupActivity {
    override fun runActivity(project: Project) {
        val projectDir = Paths.get(project.basePath)
        val walker = VisibleFilesFinder(projectDir)
        Files.walkFileTree(projectDir, walker)

        val balloonNotifications = NotificationGroup("Notification group", NotificationDisplayType.BALLOON, true)
        val resultNotification = balloonNotifications.createNotification(
            "Project finder",
            genMessage(walker),
            NotificationType.INFORMATION
        ) { _, hyperlinkEvent ->
            if (hyperlinkEvent.eventType === HyperlinkEvent.EventType.ACTIVATED) {
                val file = LocalFileSystem.getInstance().refreshAndFindFileByPath(hyperlinkEvent.description)
                val descriptor = OpenFileDescriptor(project, file!!)
                FileEditorManager.getInstance(project).openTextEditor(descriptor, false);
            }
        }
        Notifications.Bus.notify(resultNotification, project);
    }

    private fun genMessage(walker: VisibleFilesFinder): String {
        val okMessage = " <a href=\"${walker.buildFileUrl}\" target=\"blank\">project</a> </html>"
        return when (walker.projectType) {
            VisibleFilesFinder.Companion.TYPES.MAVEN -> "This is Maven${okMessage}"
            VisibleFilesFinder.Companion.TYPES.GRADLE -> "This is Gradle${okMessage}"
            VisibleFilesFinder.Companion.TYPES.OTHER -> "Opps..\nCan't detect project type"
        }
    }
}