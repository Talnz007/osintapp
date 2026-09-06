#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$DIR"

echo "=========================================="
echo "  Building Android Debug APK (KMP)        "
echo "=========================================="

./gradlew :composeApp:assembleDebug

APK_PATH="composeApp/build/outputs/apk/debug/composeApp-debug.apk"

if [ -f "$APK_PATH" ]; then
    echo ""
    echo "SUCCESS: APK built successfully at:"
    echo "  $DIR/$APK_PATH"
    echo ""

    if command -v adb >/dev/null 2>&1; then
        DEVICES=$(adb devices | grep -v "List of devices" | grep "device$" | awk '{print $1}')
        if [ -n "$DEVICES" ]; then
            echo "Connected Android device(s) detected: $DEVICES"
            echo "Installing APK to connected device..."
            adb install -r "$APK_PATH"
            echo "Launching app..."
            adb shell am start -n com.noobdevs.osint/com.noobdevs.osint.MainActivity || true
            echo "Done! App is now running on your device."
        else
            echo "No Android device detected via USB. Connect your phone with USB Debugging enabled or transfer the APK via LocalSend:"
            echo "  $DIR/$APK_PATH"
        fi
    else
        echo "ADB not found. You can transfer and install the APK via LocalSend or cable:"
        echo "  $DIR/$APK_PATH"
    fi
fi
