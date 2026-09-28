package com.example.profile.presentation.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LegalDocumentTest {

    private fun asset(name: String) = parseLegalDocument(File("../../app/src/main/assets/$name").readText())

    @Test
    fun privacy_splitsIntoNumberedSectionsWithoutOrdinals() {
        val doc = asset("privacy.md")
        assertEquals("آخر تحديث: 28 سبتمبر 2026 م", doc.updated)
        assertEquals(1, doc.intro.size)
        assertEquals(11, doc.sections.size)
        assertEquals("جمع البيانات", doc.sections.first().title)
        assertEquals("التواصل معنا", doc.sections.last().title)
        assertEquals(listOf(LegalBlock.Paragraph("باستخدامك للتطبيق، فإنك توافق على هذه السياسة.")), doc.outro)
    }

    @Test
    fun terms_bulletsGroupAndClosingLineIsOutro() {
        val doc = asset("terms.md")
        assertEquals(null, doc.updated)
        assertEquals(6, doc.sections.size)
        val bullets = doc.sections[3].blocks.filterIsInstance<LegalBlock.Bullets>().single()
        assertEquals(3, bullets.items.size)
        assertEquals(1, doc.outro.size)
    }

    @Test
    fun licenses_keepsCodeBlockAndUnnumberedHeadings() {
        val doc = asset("licenses.md")
        assertEquals("الأيقونات: Tabler Icons", doc.sections[3].title)
        val code = doc.sections[3].blocks.filterIsInstance<LegalBlock.Code>().single()
        assertTrue(code.text.startsWith("MIT License"))
    }
}
