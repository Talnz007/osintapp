@echo off
setlocal enabledelayedexpansion

echo ==========================================
echo   Building Android Debug APK (KMP)
echo ==========================================

call gradlew.bat :composeApp:assembleDebug
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Gradle build failed.
    exit /b %ERRORLEVEL%
)

set APK_PATH=composeApp\build\outputs\apk\debug\composeApp-debug.apk

if exist "%APK_PATH%" (
    echo.
    echo SUCCESS: APK built successfully at:
    echo   %cd%\%APK_PATH%
    echo.
    where adb >nul 2>nul
    if %ERRORLEVEL% equ 0 (
        echo Checking for connected Android devices...
        adb install -r "%APK_PATH%"
        adb shell am start -n com.noobdevs.osint/com.noobdevs.osint.MainActivity
        echo Done! App is now running on your device.
    ) else (
        echo Connect your phone or copy the APK to your phone:
        echo   %cd%\%APK_PATH%
    )
)
