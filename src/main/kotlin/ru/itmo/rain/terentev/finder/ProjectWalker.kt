package ru.itmo.rain.terentev.finder

import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.FileVisitResult.CONTINUE
import java.nio.file.FileVisitResult.SKIP_SUBTREE
import java.nio.file.FileVisitResult.TERMINATE

class VisibleFilesFinder(val userDir: Path) : SimpleFileVisitor<Path>() {
    companion object {
        enum class TYPES {
            GRADLE, MAVEN, OTHER
        }

        val typeMap = mapOf(
            "build.gradle" to TYPES.GRADLE,
            "pom.xml" to TYPES.MAVEN
        )
    }

    var projectType: TYPES = TYPES.OTHER;
    var buildFileUrl = ""

    override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
        return when {
            userDir.equals(dir) -> CONTINUE
            else -> SKIP_SUBTREE
        }
    }

    override fun visitFile(path: Path, attrs: BasicFileAttributes): FileVisitResult {
        val file = path.toFile()
        if (typeMap.containsKey(file.name)) {
            when {
                projectType.equals(TYPES.OTHER) -> {
                    projectType = typeMap.get(file.name)!!
                    buildFileUrl = path.toAbsolutePath().toString()
                    return CONTINUE
                }
                else -> {
                    projectType = TYPES.OTHER
                    return TERMINATE
                }
            }
        }
        return CONTINUE
    }
}
