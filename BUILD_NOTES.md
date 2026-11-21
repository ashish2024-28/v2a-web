# V2S Build Notes

## Environment used for this source refactor

The supplied project was restructured and reviewed in a source-only environment. The environment does not provide an Android SDK or a Gradle executable, so an APK build could not be executed here without falsely claiming a successful Android build.

## Expected toolchain

- JDK: 17
- Android Gradle Plugin: 9.1.1
- Gradle: 9.3.1
- compileSdk: 36
- targetSdk: 36
- minSdk: 26

## Android Studio procedure

1. Open the project root.
2. Select JDK 17 in Gradle settings.
3. Install Android SDK Platform 36.
4. Sync the project.
5. Build `app` → debug.
6. Install the APK on a test device.
7. Run the QA checklist.

## Important production note

A successful Gradle compile is necessary but not sufficient for a production media player. Test real devices, codec compatibility, permission behavior, large libraries, process death, rotation, background playback and release builds before publishing.
