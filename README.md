# Claudio

**Snap it. Ask it. Learn it.**

Claudio is an Android study companion. Photograph any question — homework, a textbook page, handwritten notes, a diagram, a drawing, a character you don't recognize — ask anything about it, and Claudio reads the image and teaches you the answer, step by step.

## Features

- **Photo-first**: take a picture with your camera or pick one from your gallery. Claudio sends the actual image to a vision AI — it reads handwriting, equations, diagrams and drawings, not just OCR text.
- **Ask anything**: quick prompts (Solve this, Explain step by step, Summarize, Translate, Make flashcards) or type your own question.
- **Real tutoring**: streaming answers rendered as rich markdown — headings, numbered steps, bold results, code blocks — with follow-up questions in the same conversation.
- **BYOK (Bring Your Own Key)**: paste an [OpenRouter](https://openrouter.ai/keys) API key, or point Claudio at any OpenAI-compatible endpoint (OpenAI, local gateways, etc.). Requests go straight from your phone to the provider.
- **Free models, automatically**: Claudio queries your key's provider and lists every free model it finds, tagged with which ones can see photos. On first setup it auto-picks a free vision model so you never burn credits by accident.
- **Live thinking trace**: reasoning models stream their thought process into a Claude-Code-style collapsible "Thinking" panel with a live timer, which folds into "Thought for Ns" once the answer begins.
- **It can draw**: ask Claudio to draw an angle, a triangle, a graph or a labeled diagram of what your photo shows — it returns a real SVG that Claudio renders as a crisp drawing on a paper card, with fullscreen view and copy-SVG.
- **Model choice**: the live free-model list, curated vision picks as offline fallback (Gemini, GPT-4o, Claude, Qwen VL, Llama Vision), or paste any model id.
- **Private history**: every conversation is saved on-device only. Reopen, continue, or delete them anytime.
- **Delightful UI**: Jetpack Compose with an animated aurora background, a blinking owl mascot, a spinning-ring camera button, staggered entrances, springy buttons, a scanning effect while your photo loads, and live typing indicators.

## Privacy

No accounts. No analytics. No servers in between. Your API key and conversations never leave your device except as direct requests to the AI provider you configure.

## Install

1. Go to [**Releases**](../../releases/latest) and download `claudio.apk` on your Android phone (Android 8.0+).
2. Open the file and allow installation from unknown sources when prompted.
3. Launch Claudio, paste your OpenRouter key, pick a model, and snap your first question.

Every push to `main` builds a fresh APK via GitHub Actions and publishes it as a release.

## Build from source

Requirements: JDK 17, Android SDK (platform 35).

```bash
./gradlew assembleDebug
# APK at app/build/outputs/apk/debug/app-debug.apk
```

Or open the project in Android Studio and press Run.

## Tech stack

- Kotlin 2.0 + Jetpack Compose (Material 3, custom dark theme)
- Camera via `ActivityResultContracts.TakePicture` + FileProvider; gallery via the Android Photo Picker
- OkHttp with SSE streaming against the OpenRouter / OpenAI-compatible chat completions API (content + reasoning deltas)
- kotlinx.serialization, DataStore (settings), JSON file storage (history)
- AndroidSVG for rendering model-generated diagrams inline
- GitHub Actions CI → installable APK on every push

## Credits

**Made by poging kyle.**

## License

MIT