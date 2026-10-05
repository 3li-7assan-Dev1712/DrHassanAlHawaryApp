package com.example.profile.presentation.components

/**
 * The legal pages (privacy.md, terms.md, licenses.md) split into the parts the screen lays out:
 * a "last updated" line, an intro, numbered sections for the contents card, and a closing line.
 *
 * Only the Markdown those files use is understood: `#`/`##` headings, `---` dividers,
 * `-`/`*` bullets, ``` fences and `**bold**`.
 */
internal data class LegalDocument(
    val updated: String?,
    val intro: List<LegalBlock>,
    val sections: List<LegalSection>,
    val outro: List<LegalBlock>,
)

internal data class LegalSection(val title: String, val blocks: List<LegalBlock>)

internal sealed interface LegalBlock {
    data class Paragraph(val text: String) : LegalBlock
    data class Bullets(val items: List<String>) : LegalBlock
    data class Code(val text: String) : LegalBlock
}

/** "أولًا: جمع البيانات" -> "جمع البيانات"; the screen numbers sections itself. */
private val ORDINAL_PREFIX = Regex(
    "^(أولًا|أولاً|ثانيًا|ثانياً|ثالثًا|ثالثاً|رابعًا|رابعاً|خامسًا|خامساً|سادسًا|سادساً|سابعًا|سابعاً|" +
        "ثامنًا|ثامناً|تاسعًا|تاسعاً|عاشرًا|عاشراً|الحادي عشر|الثاني عشر|الثالث عشر)\\s*:\\s*"
)

private const val UPDATED_PREFIX = "آخر تحديث"

internal fun parseLegalDocument(markdown: String): LegalDocument {
    var updated: String? = null
    val intro = mutableListOf<LegalBlock>()
    val sections = mutableListOf<LegalSection>()
    val outro = mutableListOf<LegalBlock>()

    var sectionTitle: String? = null
    var sectionBlocks = mutableListOf<LegalBlock>()
    // Sections end at their "---"; text after the last one is the closing line.
    var sectionClosed = false

    fun target(): MutableList<LegalBlock> = when {
        sectionTitle == null -> intro
        sectionClosed -> outro
        else -> sectionBlocks
    }

    fun add(block: LegalBlock) {
        val list = target()
        val last = list.lastOrNull()
        if (block is LegalBlock.Bullets && last is LegalBlock.Bullets) {
            list[list.lastIndex] = LegalBlock.Bullets(last.items + block.items)
        } else {
            list += block
        }
    }

    fun closeSection() {
        val title = sectionTitle ?: return
        if (!sectionClosed) sections += LegalSection(title, sectionBlocks)
        sectionBlocks = mutableListOf()
    }

    val lines = markdown.replace("\r\n", "\n").split("\n")
    var i = 0
    while (i < lines.size) {
        val line = lines[i].trim()
        when {
            line.startsWith("```") -> {
                val code = mutableListOf<String>()
                i++
                while (i < lines.size && !lines[i].trim().startsWith("```")) code += lines[i++]
                add(LegalBlock.Code(code.joinToString("\n").trimEnd()))
            }

            line.isEmpty() || line.startsWith("# ") -> Unit

            line == "---" || line == "___" -> if (sectionTitle != null && !sectionClosed) {
                closeSection()
                sectionClosed = true
            }

            line.startsWith("## ") -> {
                closeSection()
                sectionTitle = line.removePrefix("## ").trim().replace(ORDINAL_PREFIX, "")
                sectionClosed = false
            }

            line.startsWith("- ") || line.startsWith("* ") -> add(LegalBlock.Bullets(listOf(line.drop(2).trim())))

            sectionTitle == null && updated == null && line.startsWith(UPDATED_PREFIX) -> updated = line

            else -> add(LegalBlock.Paragraph(line))
        }
        i++
    }
    closeSection()

    return LegalDocument(updated, intro, sections, outro)
}
