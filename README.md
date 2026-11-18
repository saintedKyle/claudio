# Claudio

**Snap it. Ask it. Learn it.**

Claudio is a warm, photo-first study companion for Android. Photograph any question — homework, a textbook page, handwritten notes, a diagram, even a drawing — and Claudio reads it and teaches the answer step by step. You can also just type.

## Features

- **Works out of the box**: Claudio Free uses a built-in key, so there is no setup screen and no key to paste. Download the APK and start asking. BYOK (bring your own key) is optional and always takes priority when set.
- **Photo-first**: take a picture with the camera or pick one from the gallery. The actual image goes to a vision AI, so it can read handwriting, equations and diagrams.
- **Type anything**: the home composer starts a conversation with or without a photo.
- **Add photos anytime**: attach a new photo to an ongoing conversation from the chat composer, with captions if you like.
- **Real tutoring**: streaming answers rendered as rich markdown — headings, numbered steps, bold results, code blocks, math typography and follow-up questions in the same thread.
- **Structured answer cards**: every study answer is split into a highlighted **Final answer** card, a **Step by step** card, and a **Notebook** card with a one-tap copy button for exactly what to write down.
- **Live web search**: tap **Web** in the composer to ground answers in current results. Sources are cited as clickable links; can be toggled off to save the search fee.
- **Live thinking trace**: reasoning models stream their thought process into a collapsible "Thinking" panel with a live timer, which folds into "Thought for Ns" once the answer begins.
- **It can draw**: ask for an angle, triangle, graph or labelled diagram and Claudio returns a real SVG rendered as a crisp paper card with fullscreen view and copy-SVG.
- **Warm-black theme studio**: choose an accent (Ember, Amber, Sage, Ocean, Iris, Rose, Mono), a background family (Espresso, Graphite, Midnight, AMOLED), your message style, text size and reduced motion. Themes animate smoothly and persist.
- **Smooth streaming**: answer deltas are batched and the message list is pinned intelligently, so text no longer flickers or fights the scroll position while it writes.
- **Model choice**: the live free-model list from your key, curated free vision picks as an offline fallback, or paste any model id.
- **Private history**: conversations are saved on-device only. Reopen, continue or delete them anytime.

## Privacy

No accounts. No analytics. No Claudio servers in between. Conversations stay on your device. In free mode, requests go directly to OpenRouter with the built-in key; in BYOK mode they go directly to the provider you configure.

## Install

1. Get the latest `claudio.apk` from [**Releases**](../../releases/latest).
2. Open the file and allow installation from unknown sources when prompted.
3. Launch Claudio and ask your first question — no key required.

Every push to `main` builds a fresh APK via GitHub Actions and publishes it as a release.

## Use your own key

Open **Settings → Connection → Use my own key**. Paste an [OpenRouter](https://openrouter.ai/keys) key (or point Claudio at any OpenAI-compatible endpoint) and save. Tap **Switch back to the built-in free key** at any time.

## Build from source

Requirements: JDK 17, Android SDK (platform 35).

```bash
./gradlew assembleDebug
# APK at app/build/outputs/apk/debug/app-debug.apk
```

Or open the project in Android Studio and press Run.

## Tech stack

- Kotlin 2.0 + Jetpack Compose (Material 3, custom warm-black theme system with animated runtime palettes)
- Camera via `ActivityResultContracts.TakePicture` + FileProvider; gallery via the Android Photo Picker
- OkHttp with SSE streaming against the OpenRouter / OpenAI-compatible chat completions API (content + reasoning deltas, batched for Compose)
- kotlinx.serialization, DataStore (settings + theme), JSON file storage (history)
- AndroidSVG for rendering model-generated diagrams inline
- GitHub Actions CI → installable APK on every push

## Credits

**Made by kyle.**

## License

MIT
