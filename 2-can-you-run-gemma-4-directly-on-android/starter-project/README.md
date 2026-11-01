# Pocket AI — Episode 2 starter

An Android 13+ Compose app for offline Gemma 4 E2B chat with one optional image.
Uses Hilt/MVVM, LiteRT LM 0.14.0, a resumable internal-storage download, and
streaming Markdown. Messages stay in memory; clearing chat resets the model conversation.

## Build

Open this directory in Android Studio 2026.2.2, using its bundled JDK 25 and
Android SDK platform 37 (with Build Tools 36.0.0).
Create an ignored `local.properties` in this directory:

```properties
sdk.dir=C\:/Users/YOUR_USER/AppData/Local/Android/Sdk
HF_TOKEN=your_hugging_face_token
```

Accept the model's Hugging Face terms before downloading:
https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

The SDK Manager package name for this platform is `platforms;android-37.0`.

The token is embedded in debug builds only. Do not distribute a debug APK containing
your personal token. Release builds use an empty token; gated downloads require
an appropriate production credential flow before distributing a release.

The model download needs network access and sufficient free internal storage.
After a verified complete download, inference uses the CPU locally without a cloud API.
Runtime model compatibility and performance must be checked on a supported physical
device. No model is bundled and no model or token is needed for compilation.
