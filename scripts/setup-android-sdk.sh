#!/usr/bin/env bash
# Setup script to provision a minimal Android SDK and pre-resolve Gradle dependencies
# for offline/limited-network build phases. Designed for non-interactive CI/agent
# environments. Run during the workspace setup phase before executing Gradle tasks.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-"$HOME/android-sdk"}"
ANDROID_HOME="$ANDROID_SDK_ROOT"
CMDLINE_TOOLS_VERSION="10406996"
CMDLINE_TOOLS_ZIP="commandlinetools-linux-${CMDLINE_TOOLS_VERSION}_latest.zip"
CMDLINE_TOOLS_URL="https://dl.google.com/android/repository/${CMDLINE_TOOLS_ZIP}"
SDK_MANAGER="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager"
PLATFORM_VERSION="36"
BUILD_TOOLS_VERSION="36.0.0"

log() {
    echo "[setup-android-sdk] $*"
}

install_prereqs() {
    local packages=(curl unzip)
    local missing=()
    for pkg in "${packages[@]}"; do
        if ! command -v "$pkg" >/dev/null 2>&1; then
            missing+=("$pkg")
        fi
    done

    if [[ ${#missing[@]} -gt 0 ]]; then
        log "Installing prerequisites: ${missing[*]}"
        export DEBIAN_FRONTEND=noninteractive
        apt-get update -y >/dev/null
        apt-get install -y "${missing[@]}" >/dev/null
    fi
}

download_cmdline_tools() {
    if [[ -x "$SDK_MANAGER" ]]; then
        log "Command-line tools already present at $SDK_MANAGER"
        return
    fi

    log "Downloading Android command-line tools (${CMDLINE_TOOLS_VERSION})"
    local tmp_dir
    tmp_dir="$(mktemp -d)"
    trap 'rm -rf "${tmp_dir}"' EXIT

    curl -sSL "$CMDLINE_TOOLS_URL" -o "$tmp_dir/$CMDLINE_TOOLS_ZIP"
    mkdir -p "$ANDROID_SDK_ROOT/cmdline-tools"
    unzip -q "$tmp_dir/$CMDLINE_TOOLS_ZIP" -d "$tmp_dir"

    # Ensure the expected cmdline-tools/latest layout for sdkmanager
    rm -rf "$ANDROID_SDK_ROOT/cmdline-tools/latest"
    mv "$tmp_dir/cmdline-tools" "$ANDROID_SDK_ROOT/cmdline-tools/latest"

    log "Installed command-line tools to $ANDROID_SDK_ROOT/cmdline-tools/latest"
}

install_sdk_components() {
    log "Installing minimal SDK components (platform-tools, platform ${PLATFORM_VERSION}, build-tools ${BUILD_TOOLS_VERSION})"
    yes | "$SDK_MANAGER" --sdk_root="$ANDROID_SDK_ROOT" --licenses >/dev/null

    "$SDK_MANAGER" --sdk_root="$ANDROID_SDK_ROOT" --install \
        "platform-tools" \
        "platforms;android-${PLATFORM_VERSION}" \
        "build-tools;${BUILD_TOOLS_VERSION}"
}

write_local_properties() {
    local local_props="$REPO_ROOT/local.properties"
    log "Writing sdk.dir to $local_props"
    cat >"$local_props" <<'EOF_LOCAL'
sdk.dir=$ANDROID_SDK_ROOT
EOF_LOCAL
}

warmup_gradle_dependencies() {
    log "Pre-resolving Gradle dependencies to warm the cache"
    (cd "$REPO_ROOT" && ANDROID_SDK_ROOT="$ANDROID_SDK_ROOT" ANDROID_HOME="$ANDROID_HOME" ./gradlew --no-daemon resolveAllDependencies)
}

main() {
    install_prereqs
    mkdir -p "$ANDROID_SDK_ROOT"
    download_cmdline_tools
    install_sdk_components
    write_local_properties
    warmup_gradle_dependencies

    log "Android SDK setup complete. ANDROID_SDK_ROOT=$ANDROID_SDK_ROOT"
}

main "$@"
