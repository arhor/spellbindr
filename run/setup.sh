#!/usr/bin/env bash

PLATFORM_VERSION="36"
BLD_TOOL_VERSION="36.0.0"
CMDLINE_TOOLS_VERSION="13114758"
CMDLINE_TOOLS_ZIP="commandlinetools-linux-${CMDLINE_TOOLS_VERSION}_latest.zip"
CMDLINE_TOOLS_URL="https://dl.google.com/android/repository/${CMDLINE_TOOLS_ZIP}"
SDK_MANAGER="${ANDROID_HOME}/cmdline-tools/bin/sdkmanager"

echo "Getting Android Studio"
wget -O android-commandlinetools.zip "${CMDLINE_TOOLS_URL}"

echo "Unpacking Android Studio"
unzip android-commandlinetools.zip -d "${ANDROID_HOME}"

echo "Updating sdkmanager"
"$SDK_MANAGER" --sdk_root="${ANDROID_HOME}" --update

echo "Installing Android SDK"
yes | "$SDK_MANAGER" --sdk_root="${ANDROID_HOME}" \
    "platform-tools" \
    "platforms;android-${PLATFORM_VERSION}" \
    "build-tools;${BLD_TOOL_VERSION}"

echo "Git Submodule Init"
git submodule update --init --recursive
