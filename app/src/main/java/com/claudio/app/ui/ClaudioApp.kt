package com.claudio.app.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.claudio.app.ClaudioViewModel
import com.claudio.app.ui.components.AuroraBackground
import com.claudio.app.ui.components.OwlLogo
import com.claudio.app.ui.screens.ChatScreen
import com.claudio.app.ui.screens.HistoryScreen
import com.claudio.app.ui.screens.HomeScreen
import com.claudio.app.ui.screens.PreviewScreen
import com.claudio.app.ui.screens.SettingsScreen
import com.claudio.app.ui.screens.SetupScreen
import java.io.File

@Composable
fun ClaudioApp(vm: ClaudioViewModel = viewModel()) {
    val settings by vm.settings.collectAsState()
    val context = LocalContext.current
    var screen by rememberSaveable { mutableStateOf("home") }
    var captureUri by rememberSaveable { mutableStateOf<String?>(null) }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val uri = captureUri
        if (ok && uri != null) {
            vm.loadImage(Uri.parse(uri))
            screen = "preview"
        }
    }
    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            vm.loadImage(uri)
            screen = "preview"
        }
    }
    val launchCamera: () -> Unit = {
        val dir = File(context.cacheDir, "captures").apply { mkdirs() }
        val file = File(dir, "cap_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        captureUri = uri.toString()
        takePicture.launch(uri)
    }

    LaunchedEffect(Unit) {
        if (screen == "preview" && vm.pendingImage.value == null && captureUri != null) {
            vm.loadImage(Uri.parse(captureUri))
        }
    }

    when {
        !settings.loaded -> {
            AuroraBackground {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    OwlLogo(size = 120.dp)
                }
            }
        }
        settings.apiKey.isBlank() -> {
            SetupScreen(onStart = { key, base, model -> vm.saveSettings(key, base, model) })
        }
        else -> {
            AnimatedContent(
                targetState = screen,
                transitionSpec = {
                    (fadeIn(tween(260)) + slideInHorizontally(tween(260)) { it / 8 }) togetherWith
                        (fadeOut(tween(180)) + slideOutHorizontally(tween(260)) { -it / 8 })
                },
                label = "screen"
            ) { name ->
                when (name) {
                    "preview" -> PreviewScreen(
                        vm = vm,
                        onAsk = { question -> vm.startSession(question); screen = "chat" },
                        onRetake = launchCamera,
                        onBack = { screen = "home" }
                    )
                    "chat" -> ChatScreen(vm = vm, onBack = { screen = "home" })
                    "history" -> HistoryScreen(
                        vm = vm,
                        onOpen = { id -> vm.openSession(id); screen = "chat" },
                        onBack = { screen = "home" }
                    )
                    "settings" -> SettingsScreen(vm = vm, onBack = { screen = "home" })
                    else -> HomeScreen(
                        vm = vm,
                        onCamera = launchCamera,
                        onGallery = { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        onOpenSession = { id -> vm.openSession(id); screen = "chat" },
                        onHistory = { screen = "history" },
                        onSettings = { screen = "settings" }
                    )
                }
            }
        }
    }
}