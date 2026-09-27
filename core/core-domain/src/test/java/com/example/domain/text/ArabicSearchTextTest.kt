package com.example.domain.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ArabicSearchTextTest {

    private fun matches(word: String, query: String) = ArabicSearchText.matches(word, ArabicSearchText.queryTerms(query))

    @Test
    fun `hamza and article variants match`() {
        assertTrue(matches("أحكام", "احكام"))
        assertTrue(matches("صيام", "الصيام"))
        assertTrue(matches("والصيام", "الصيام"))
        assertTrue(matches("بالصَّلاةِ", "صلاه"))
        assertTrue(matches("مسألة", "مساله"))
        assertTrue(matches("وقت", "وقت"))
    }

    @Test
    fun `the bare article matches nothing and is not searchable`() {
        assertFalse(matches("الصيام", "ال"))
        assertFalse(ArabicSearchText.isSearchable("ال"))
        assertFalse(ArabicSearchText.isSearchable("ا"))
        assertFalse(ArabicSearchText.isSearchable("  "))
        assertTrue(ArabicSearchText.isSearchable("ال زكاة"))
    }

    @Test
    fun `highlights are whole words, never fragments`() {
        val text = "أحكام الصيام والصيام الواجب"
        val ranges = ArabicSearchText.highlightRanges(text, "صيام")
        assertEquals(listOf("الصيام", "والصيام"), ranges.map { text.substring(it.first, it.last + 1) })
        // "قيام" contains "يام" but doesn't start with it: no fragment highlight.
        assertTrue(ArabicSearchText.highlightRanges("قيام الليل", "يام").isEmpty())
    }

    @Test
    fun `snippet centers on the first match, snaps to words, marks cuts`() {
        val words = List(60) { "كلمة" }
        val text = (words.take(30) + "الزكاة" + words.drop(30)).joinToString(" ")
        val snippet = ArabicSearchText.snippet(text, "زكاه", length = 60)
        assertTrue(snippet.text, snippet.text.startsWith("… "))
        assertTrue(snippet.text, snippet.text.endsWith(" …"))
        assertTrue(snippet.text, snippet.text.contains("الزكاة"))
        assertFalse(snippet.text, snippet.text.contains("كلم …") || snippet.text.contains("… لمة"))
        assertEquals(1, snippet.highlights.size)
    }

    @Test
    fun `short text without a match is returned whole`() {
        val snippet = ArabicSearchText.snippet("نص قصير", "زكاة")
        assertEquals("نص قصير", snippet.text)
        assertTrue(snippet.highlights.isEmpty())
    }
}
