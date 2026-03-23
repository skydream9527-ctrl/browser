@echo off
chcp 65001 >nul
echo Building Browser APK...
echo ===========================

REM Check if Android SDK exists
if not exist "%ANDROID_HOME%" (
    echo ERROR: ANDROID_HOME not set or SDK not found
    echo Please install Android SDK and set ANDROID_HOME environment variable
    exit /b 1
)

REM Check for Gradle
set GRADLE_CMD=gradle
where /q gradle
if errorlevel 1 (
    echo ERROR: Gradle not found in PATH
    echo Please install Gradle or use Android Studio
    exit /b 1
)

echo Cleaning previous build...
call gradle clean
echo.

echo Building Debug APK...
call gradle assembleDebug --stacktrace
if errorlevel 1 (
    echo ERROR: Build failed
    exit /b 1
)

echo.
echo Build successful!
echo APK location: app\build\outputs\apk\debug\app-debug.apk

pause