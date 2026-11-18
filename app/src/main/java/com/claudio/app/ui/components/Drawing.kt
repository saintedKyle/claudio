package com.claudio.app.ui.components

import android.graphics.Bitmap
import android.graphics.RectF
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.claudio.app.ui.theme.Amber
import com.claudio.app.ui.theme.Cyan
import com.claudio.app.ui.theme.InkDeep
import com.claudio.app.ui.theme.Surface2
import com.claudio.app.ui.theme.TextMuted
import com.claudio.app.ui.theme.TextPrimary
import com.caverock.androidsvg.SVG
import kotlin.math.roundToInt

private val Paper = Color(0xFFFDFBF3)

private data class RenderedDrawing(val image: ImageBitmap, val aspect: Float)

private fun renderSvg(source: String): RenderedDrawing? = runCatching {
    val cleaned = source
        .replace(Regex("<script[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), "")
        .replace(Regex("on\\w+\\s*=\\s*\"[^\"]*\"", RegexOption.IGNORE_CASE), "")
    val svg = SVG.getFromString(cleaned)
    val box = svg.documentViewBox
    val aspect = (if (box != null && box.height() > 0f) box.width() / box.height() else 4f / 3f).coerceIn(0.4f, 3f)
    svg.setDocumentWidth(1000f)
    svg.setDocumentHeight(1000f / aspect)
    val picture = svg.renderToPicture()
    val width = 1000
    val height = (1000f / aspect).roundToInt().coerceAtLeast(1)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawPicture(picture, null, RectF(0f, 0f, width.toFloat(), height.toFloat()))
    RenderedDrawing(bitmap.asImageBitmap(), aspect)
}.getOrNull()

@Composable
fun DrawingCard(svgSource: String) {
    val drawing = remember(svgSource) { renderSvg(svgSource) }
    var fullscreen by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current

    if (drawing == null) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Surface2)
                .padding(14.dp)
        ) {
            Text("Claudio tried to draw, but the SVG couldn't be rendered:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
            Text(svgSource, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Cyan)
        }
        return
    }

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface2)
            .padding(10.dp)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(drawing.aspect)
                .clip(RoundedCornerShape(10.dp))
                .background(Paper)
                .clickable { fullscreen = true }
        ) {
            Image(
                bitmap = drawing.image,
                contentDescription = "Claudio's drawing",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 8.dp, start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Claudio's drawing", style = MaterialTheme.typography.labelMedium, color = Amber, modifier = Modifier.weight(1f))
            Icon(
                Icons.Filled.ContentCopy,
                "Copy SVG",
                tint = TextMuted,
                modifier = Modifier.size(18.dp).clip(RoundedCornerShape(6.dp)).clickable {
                    clipboard.setText(AnnotatedString(svgSource))
                }
            )
            Spacer(Modifier.width(14.dp))
            Icon(
                Icons.Filled.Fullscreen,
                "View full screen",
                tint = TextMuted,
                modifier = Modifier.size(18.dp).clip(RoundedCornerShape(6.dp)).clickable { fullscreen = true }
            )
        }
    }

    if (fullscreen) {
        Dialog(
            onDismissRequest = { fullscreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(InkDeep.copy(alpha = 0.97f))
                    .clickable { fullscreen = false },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(20.dp)) {
                    Box(
                        Modifier
                            .fillMaxWidth(0.94f)
                            .aspectRatio(drawing.aspect)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Paper)
                    ) {
                        Image(
                            bitmap = drawing.image,
                            contentDescription = "Claudio's drawing, full screen",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Row(Modifier.padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.FullscreenExit, null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Tap anywhere to close", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                    }
                }
            }
        }
    }
}