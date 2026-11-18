# Claudio

**Snap it. Ask it. Learn it.**

Claudio is an Android study companion. Photograph any question — homework, a textbook page, handwritten notes, a diagram, a drawing, a character you don't recognize — ask anything about it, and Claudio reads the image and teaches you the answer, step by step.

## Features

- **Photo-first**: take a picture with your camera or pick one from your gallery. Claudio sends the actual image to a vision AI — it reads handwriting, equations, diagrams and drawings, not just OCR text.
- **Ask anything**: quick prompts (Solve this, Explain step by step, Summarize, Translate, Make flashcards) or type your own question.
- **Real tutoring**: streaming answers rendered as rich markdown — headings, numbered steps, bold results, code blocks — with follow-up questions in the same conversation.
- **BYOK (Bring Your Own Key)**: paste an [OpenRouter](https://openrouter.ai/keys) API key, or point Claudio at any OpenAI-compatible endpoint (OpenAI, local gateways, etc.). Requests go straight from your phone to the provider.
- **Model choice**: curated vision models (Gemini 2.0/2.5 Flash, GPT-4o / 4o-mini, Claude 3.5 Sonnet, Qwen 2.5 VL, Llama 3.2 Vision) or paste any model id.
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
- OkHttp with SSE streaming against the OpenRouter / OpenAI-compatible chat completions API
- kotlinx.serialization, DataStore (settings), JSON file storage (history)
- GitHub Actions CI → installable APK on every push

## Credits

**Made by poging kyle.**

## License

MIT