package com.example.core.ui

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Lint-style guard: no string resource in ANY module may contain tatweel/kashida
 * (U+0640). Stretched words ("المـــقالات") render unevenly and differently per
 * font; the design uses plain text everywhere.
 *
 * Runs as a plain JVM test from core-ui but scans every XML file in every
 * `res/values*` folder of the repository - not just ones named strings.xml
 * (the app module keeps its strings in `string.xml`).
 */
class NoTatweelInStringsTest {

    @Test
    fun `no string resource contains tatweel`() {
        val root = generateSequence(File("").absoluteFile) { it.parentFile }
            .first { File(it, "settings.gradle.kts").exists() }

        val stringFiles = root.walkTopDown()
            .onEnter { dir -> dir.name != "build" && dir.name != ".gradle" && !dir.name.startsWith(".") }
            .filter { it.isFile && it.extension == "xml" && it.parentFile.name.startsWith("values") }
            .toList()
        assertTrue("found no values XML under $root - is the scan path right?", stringFiles.isNotEmpty())

        val offenders = stringFiles.flatMap { file ->
            file.readLines(Charsets.UTF_8).mapIndexedNotNull { i, line ->
                if (TATWEEL in line) "${file.relativeTo(root)}:${i + 1}: ${line.trim()}" else null
            }
        }
        assertTrue(
            "Tatweel (U+0640) found in string resources - remove it:\n" + offenders.joinToString("\n"),
            offenders.isEmpty(),
        )
    }

    /**
     * Tanween fath goes on the letter before the alif ("قريبًا"), not on the alif ("قريباً"):
     * the second form renders the mark in the wrong place in Cairo.
     */
    @Test
    fun `no string resource puts tanween on the alif`() {
        val offenders = valuesXmlFiles().flatMap { file ->
            file.readLines(Charsets.UTF_8).mapIndexedNotNull { i, line ->
                if (TANWEEN_ON_ALIF in line) "${file.relativeTo(root)}:${i + 1}: ${line.trim()}" else null
            }
        }
        assertTrue(
            "Write \"ًا\" instead of \"اً\" in string resources:\n" + offenders.joinToString("\n"),
            offenders.isEmpty(),
        )
    }

    private val root: File
        get() = generateSequence(File("").absoluteFile) { it.parentFile }
            .first { File(it, "settings.gradle.kts").exists() }

    private fun valuesXmlFiles(): List<File> = root.walkTopDown()
        .onEnter { dir -> dir.name != "build" && dir.name != ".gradle" && !dir.name.startsWith(".") }
        .filter { it.isFile && it.extension == "xml" && it.parentFile.name.startsWith("values") }
        .toList()

    private companion object {
        const val TATWEEL = 'ـ'
        const val TANWEEN_ON_ALIF = "اً"
    }
}
