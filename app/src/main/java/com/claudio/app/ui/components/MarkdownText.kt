package com.claudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.claudio.app.ui.theme.Amber
import com.claudio.app.ui.theme.Cyan
import com.claudio.app.ui.theme.Surface2
import com.claudio.app.ui.theme.Surface3
import com.claudio.app.ui.theme.TextPrimary
import com.claudio.app.ui.theme.TextSecondary
import com.claudio.app.ui.theme.Violet

private val MathInk = Color(0xFFFFE0B0)
private val MathPlate = Color(0x14F59E0B)
private val MathPlateEdge = Color(0x59F59E0B)

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
        val next = text.indexOfAny(charArrayOf('$', '*', '`'), i)
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
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = TextPrimary)) {
                        append(inlineMarkdown(text.substring(i + 2, end)))
                    }
                    i = end + 2
                } else { append(text[i]); i++ }
            }
            text[i] == '`' -> {
                val end = text.indexOf('`', i + 1)
                if (end > i + 1) {
                    withStyle(SpanStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = Cyan, background = Surface3)) {
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
            else -> { append(text[i]); i++ }
        }
    }
}

@Composable
fun MarkdownText(markdown: String, modifier: Modifier = Modifier, live: Boolean = false) {
    val blocks = remember(markdown) { parseBlocks(normalizeRichText(markdown)) }
    SelectionContainer {
        Column(modifier, verticalArrangement = Arrangement.spacedBy(9.dp)) {
            blocks.forEach { block ->
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
                                        .background(Amber)
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
                            .background(MathPlate)
                            .border(1.dp, MathPlateEdge, RoundedCornerShape(14.dp))
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
                            color = Amber,
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
                        Box(Modifier.width(3.dp).fillMaxHeight().clip(RoundedCornerShape(50)).background(Violet))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            inlineMarkdown(block.text),
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            color = TextSecondary
                        )
                    }
                    is Block.Rule -> Box(
                        Modifier.fillMaxWidth().height(1.dp).background(Surface3)
                    )
                }
            }
        }
    }
}