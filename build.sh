#!/bin/bash
set -e

echo "Building Browser APK..."
echo "==========================="

# Check if Android SDK exists
if [ -z "$ANDROID_HOME" ] || [ ! -d "$ANDROID_HOME" ]; then
    echo "ERROR: ANDROID_HOME not set or SDK not found"
    echo "Please install Android SDK and set ANDROID_HOME environment variable"
    exit 1
fi

# Check for Gradle
if ! command -v gradle &> /dev/null; then
    echo "ERROR: Gradle not found in PATH"
    echo "Please install Gradle or use Android Studio"
    exit 1
fi

echo "Cleaning previous build..."
gradle clean

echo ""
echo "Building Debug APK..."
gradle assembleDebug --stacktrace

echo ""
echo "Build successful!"
echo "APK location: app/build/outputs/apk/debug/app-debug.apk"