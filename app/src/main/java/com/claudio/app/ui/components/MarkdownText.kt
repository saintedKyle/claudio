package com.claudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.claudio.app.ui.theme.Cyan
import com.claudio.app.ui.theme.LocalClaudioColors
import com.claudio.app.ui.theme.Surface2
import com.claudio.app.ui.theme.Surface3
import com.claudio.app.ui.theme.TextPrimary
import com.claudio.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay

private val MathInk = Color(0xFFFFE0B0)
private val InlineCodeInk = Color(0xFFB9C9D2)
private val InlineCodePlate = Color(0x1AFFFFFF)
private val LinkInk = Color(0xFF9FC3DE)

private sealed interface Block {
    data class Heading(val level: Int, val text: String) : Block
    data class Para(val text: String) : Block
    data class Code(val text: String, val lang: String) : Block
    data class Math(val text: String) : Block
    data class ListItem(val marker: String, val text: String) : Block
    data class Quote(val text: String) : Block
    data object Rule : Block
}

private fun parseBlocks(markdown: String): List<Block> {
    val blocks = mutableListOf<Block>()
    val paragraph = StringBuilder()
    var inCode = false
    var codeLang = ""
    val code = StringBuilder()

    fun flushParagraph() {
        if (paragraph.isNotBlank()) blocks.add(Block.Para(paragraph.toString().trim()))
        paragraph.clear()
    }

    markdown.lines().forEach { raw ->
        val line = raw.trimEnd()
        if (line.trim().startsWith("```")) {
            if (inCode) {
                val content = code.toString().trimEnd()
                when (codeLang) {
                    "math", "latex", "tex" -> blocks.add(Block.Math(content))
                    else -> blocks.add(Block.Code(content, codeLang))
                }
                code.clear()
                codeLang = ""
            } else {
                flushParagraph()
                codeLang = line.trim().removePrefix("```").trim().lowercase()
            }
            inCode = !inCode
            return@forEach
        }
        if (inCode) {
            code.appendLine(raw)
            return@forEach
        }
        val trimmed = line.trim()
        val heading = Regex("^(#{1,6})\\s+(.*)$").find(trimmed)
        val listItem = Regex("^([-*•]|\\d+[.)])\\s+(.*)$").find(trimmed)
        when {
            trimmed.isEmpty() -> flushParagraph()
            heading != null -> {
                flushParagraph()
                blocks.add(Block.Heading(heading.groupValues[1].length, heading.groupValues[2]))
            }
            listItem != null -> {
                flushParagraph()
                val marker = listItem.groupValues[1]
                blocks.add(Block.ListItem(if (marker.first().isDigit()) marker else "•", listItem.groupValues[2]))
            }
            trimmed.startsWith(">") -> {
                flushParagraph()
                blocks.add(Block.Quote(trimmed.removePrefix(">").trim()))
            }
            Regex("^(-{3,}|\\*{3,}|_{3,})$").matches(trimmed) -> {
                flushParagraph()
                blocks.add(Block.Rule)
            }
            else -> {
                if (paragraph.isNotEmpty()) paragraph.append(' ')
                paragraph.append(trimmed)
            }
        }
    }
    if (inCode && code.isNotBlank()) {
        val content = code.toString().trimEnd()
        if (codeLang == "math" || codeLang == "latex" || codeLang == "tex") blocks.add(Block.Math(content))
        else blocks.add(Block.Code(content, codeLang))
    }
    flushParagraph()
    return blocks
}

private val mathSpan = SpanStyle(
    fontFamily = FontFamily.Serif,
    fontStyle = FontStyle.Italic,
    fontWeight = FontWeight.Medium,
    color = MathInk
)

private fun inlineMarkdown(text: String): AnnotatedString = buildAnnotatedString {
    var i = 0
    while (i < text.length) {
        if (text[i] == '$') {
            val delim = if (text.startsWith("$$", i)) "$$" else "$"
            val end = text.indexOf(delim, i + delim.length)
            if (end > i + delim.length) {
                val inner = text.substring(i + delim.length, end)
                if (inner.isNotBlank() && inner == inner.trim()) {
                    withStyle(mathSpan) { append(latexToUnicode(inner)) }
                    i = end + delim.length
                    continue
                }
            }
        }
        val next = text.indexOfAny(charArrayOf('$', '*', '`', '['), i)
        if (next < 0) {
            append(latexToUnicode(text.substring(i)))
            break
        }
        if (next > i) {
            append(latexToUnicode(text.substring(i, next)))
            i = next
            if (text[i] == '$') continue
        }
        when {
            text.startsWith("**", i) -> {
                val end = text.indexOf("**", i + 2)
                if (end > i + 2) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(inlineMarkdown(text.substring(i + 2, end)))
                    }
                    i = end + 2
                } else { append(text[i]); i++ }
            }
            text[i] == '`' -> {
                val end = text.indexOf('`', i + 1)
                if (end > i + 1) {
                    withStyle(SpanStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = InlineCodeInk, background = InlineCodePlate)) {
                        append(" ${text.substring(i + 1, end)} ")
                    }
                    i = end + 1
                } else { append(text[i]); i++ }
            }
            text[i] == '*' -> {
                val end = text.indexOf('*', i + 1)
                if (end > i + 1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(inlineMarkdown(text.substring(i + 1, end))) }
                    i = end + 1
                } else { append(text[i]); i++ }
            }
            text[i] == '[' -> {
                val close = text.indexOf(']', i + 1)
                val end = if (close > i + 1 && close + 1 < text.length && text[close + 1] == '(') {
                    text.indexOf(')', close + 2)
                } else -1
                val url = if (end > close + 2) text.substring(close + 2, end).trim() else ""
                if (end > close + 2 && (url.startsWith("http://") || url.startsWith("https://"))) {
                    withLink(
                        LinkAnnotation.Url(
                            url = url,
                            styles = TextLinkStyles(
                                style = SpanStyle(color = LinkInk, textDecoration = TextDecoration.Underline)
                            )
                        )
                    ) {
                        append(inlineMarkdown(text.substring(i + 1, close)))
                    }
                    i = end + 1
                } else { append(text[i]); i++ }
            }
            else -> { append(text[i]); i++ }
        }
    }
}

private enum class SectionKind(val label: String) {
    Answer("Final answer"),
    Steps("Step by step"),
    Notebook("Write this in your notebook"),
    Explanation("Explanation"),
    Solution("Worked solution"),
    Sources("Sources")
}

/** Maps a markdown heading like "### 📓 Notebook" to a styled section. */
private fun sectionKindFor(title: String): SectionKind? {
    val t = title.lowercase()
        .replace(Regex("[^a-z0-9 ]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
    return when {
        t.contains("notebook") || t.contains("write this") || t.contains("copy this") -> SectionKind.Notebook
        t.contains("source") || t.contains("reference") -> SectionKind.Sources
        t.contains("answer") || t.contains("result") -> SectionKind.Answer
        t.contains("step") || t.contains("working") || t.contains("method") -> SectionKind.Steps
        t.contains("solution") -> SectionKind.Solution
        t.contains("explanation") || t.contains("explain") || t.contains("reason") -> SectionKind.Explanation
        else -> null
    }
}

private sealed interface Node {
    data class Single(val block: Block) : Node
    data class Section(val kind: SectionKind, val blocks: List<Block>) : Node
}

/**
 * Groups a recognized heading with everything up to the next heading, so the
 * renderer can draw answer/step/notebook cards instead of loose paragraphs.
 */
private fun groupNodes(blocks: List<Block>): List<Node> {
    val nodes = mutableListOf<Node>()
    var i = 0
    while (i < blocks.size) {
        val block = blocks[i]
        val kind = (block as? Block.Heading)?.let { sectionKindFor(it.text) }
        if (kind != null) {
            val content = mutableListOf<Block>()
            var j = i + 1
            while (j < blocks.size && blocks[j] !is Block.Heading) {
                content.add(blocks[j])
                j++
            }
            if (content.isNotEmpty()) {
                nodes.add(Node.Section(kind, content))
                i = j
                continue
            }
        }
        nodes.add(Node.Single(block))
        i++
    }
    return nodes
}

private fun plainNote(blocks: List<Block>): String = buildString {
    blocks.forEach { block ->
        val line = when (block) {
            is Block.Heading -> block.text
            is Block.Para -> block.text
            is Block.ListItem -> "${block.marker} ${block.text}"
            is Block.Quote -> block.text
            is Block.Math -> block.text
            is Block.Code -> block.text
            Block.Rule -> "---"
        }
        appendLine(line.replace("**", "").replace("`", "").replace("$", "").trim())
    }
}.trim()

@Composable
fun MarkdownText(markdown: String, modifier: Modifier = Modifier, live: Boolean = false) {
    val nodes = remember(markdown) { groupNodes(parseBlocks(normalizeRichText(markdown))) }
    val content: @Composable () -> Unit = {
        Column(modifier, verticalArrangement = Arrangement.spacedBy(9.dp)) {
            nodes.forEach { node ->
                when (node) {
                    is Node.Section -> SectionCard(node.kind, node.blocks, live)
                    is Node.Single -> BlockView(node.block, live)
                }
            }
        }
    }
    // Selecting live text makes streaming answers re-layout constantly; only
    // enable selection once the answer has settled.
    if (live) content() else SelectionContainer { content() }
}

@Composable
private fun SectionCard(kind: SectionKind, blocks: List<Block>, live: Boolean) {
    val colors = LocalClaudioColors.current
    val accent = colors.accent
    val shape = RoundedCornerShape(18.dp)
    val background: Color
    val edge: Color
    val header: Color
    val icon: ImageVector
    when (kind) {
        SectionKind.Answer -> {
            background = accent.copy(alpha = 0.12f)
            edge = accent.copy(alpha = 0.42f)
            header = accent
            icon = Icons.Filled.CheckCircle
        }
        SectionKind.Steps -> {
            background = colors.surface1
            edge = colors.outline
            header = colors.textSecondary
            icon = Icons.Filled.FormatListNumbered
        }
        SectionKind.Notebook -> {
            background = colors.surface2
            edge = accent.copy(alpha = 0.32f)
            header = accent
            icon = Icons.Filled.MenuBook
        }
        SectionKind.Explanation -> {
            background = colors.surface1
            edge = colors.outline
            header = colors.info
            icon = Icons.Filled.Lightbulb
        }
        SectionKind.Solution -> {
            background = colors.surface1
            edge = colors.outline
            header = colors.success
            icon = Icons.Filled.Calculate
        }
        SectionKind.Sources -> {
            background = colors.surface1
            edge = colors.outline
            header = colors.info
            icon = Icons.Filled.Public
        }
    }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(background)
            .border(1.dp, edge, shape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = header, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                kind.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = header
            )
            Spacer(Modifier.weight(1f))
            if (kind == SectionKind.Notebook) {
                CopyNotesButton(blocks)
            }
        }
        blocks.forEach { BlockView(it, live) }
    }
}

@Composable
private fun CopyNotesButton(blocks: List<Block>) {
    val colors = LocalClaudioColors.current
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(1600)
            copied = false
        }
    }
    Row(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(colors.surface3)
            .clickable {
                clipboard.setText(AnnotatedString(plainNote(blocks)))
                copied = true
            }
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (copied) Icons.Filled.Check else Icons.Filled.ContentCopy,
            contentDescription = "Copy notes",
            tint = if (copied) colors.success else colors.textMuted,
            modifier = Modifier.size(12.dp)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            if (copied) "Copied" else "Copy",
            style = MaterialTheme.typography.labelSmall,
            color = if (copied) colors.success else colors.textMuted
        )
    }
}

@Composable
private fun BlockView(block: Block, live: Boolean) {
    val colors = LocalClaudioColors.current
    val accent = colors.accent
    when (block) {
        is Block.Heading -> {
            val size = when (block.level) { 1 -> 21.sp; 2 -> 18.sp; else -> 16.sp }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (block.level <= 2) {
                    Box(
                        Modifier
                            .width(4.dp)
                            .height(if (block.level == 1) 22.dp else 18.dp)
                            .clip(RoundedCornerShape(50))
                            .background(accent)
                    )
                    Spacer(Modifier.width(10.dp))
                }
                Text(
                    inlineMarkdown(block.text),
                    fontSize = size,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    lineHeight = size * 1.3
                )
            }
        }
        is Block.Para -> Text(
            inlineMarkdown(block.text),
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 25.sp),
            color = TextPrimary
        )
        is Block.Math -> Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(accent.copy(alpha = 0.08f))
                .border(1.dp, accent.copy(alpha = 0.32f), RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp, vertical = 13.dp)
        ) {
            Text(
                latexToUnicode(block.text),
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontSize = 17.sp,
                lineHeight = 27.sp,
                color = MathInk,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        is Block.Code -> {
            when {
                block.lang == "svg-pending" -> DrawingPending(live)
                block.lang == "svg" || block.text.trimStart().startsWith("<svg") -> DrawingCard(block.text)
                else -> Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Surface2)
                        .border(1.dp, colors.outline, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        block.text,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = Cyan,
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    )
                }
            }
        }
        is Block.ListItem -> Row {
            Text(
                block.marker,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = accent,
                modifier = Modifier.width(if (block.marker.length > 1) 30.dp else 18.dp)
            )
            Text(
                inlineMarkdown(block.text),
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 25.sp),
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
        }
        is Block.Quote -> Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(Modifier.width(3.dp).fillMaxHeight().clip(RoundedCornerShape(50)).background(accent))
            Spacer(Modifier.width(12.dp))
            Text(
                inlineMarkdown(block.text),
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = TextSecondary
            )
        }
        is Block.Rule -> Box(
            Modifier.fillMaxWidth().height(1.dp).background(colors.outline)
        )
    }
}