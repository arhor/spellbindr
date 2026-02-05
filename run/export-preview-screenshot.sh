#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'EOF'
Export Compose preview screenshots to a clean output folder.

This uses Android's Compose Screenshot Testing (AGP screenshot plugin) and runs:
  ./gradlew :app:updateDebugScreenshotTest --tests '<pattern>'

Required:
  --tests <pattern>    Gradle test filter (supports '*'), e.g. '*AppTopBar*'

Optional:
  --variant <name>     Only 'debug' is supported (default: debug)
  --out <path>         Output base directory (default: app/build/outputs/preview-screenshots)
  --skip-gradle        Skip running Gradle; only export from existing reference images
  -h, --help           Show this help

Examples:
  run/export-preview-screenshot.sh --tests '*AppTopBar*'
  run/export-preview-screenshot.sh --tests '*SmokeScreenshotPreviews*'
EOF
}

tests_filter=""
variant="debug"
out_base="app/build/outputs/preview-screenshots"
skip_gradle="false"

while [ $# -gt 0 ]; do
  case "$1" in
    --tests)
      tests_filter="${2:-}"
      shift 2
      ;;
    --variant)
      variant="${2:-}"
      shift 2
      ;;
    --out)
      out_base="${2:-}"
      shift 2
      ;;
    --skip-gradle)
      skip_gradle="true"
      shift 1
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage >&2
      exit 2
      ;;
  esac
done

if [ -z "$tests_filter" ]; then
  echo "--tests is required" >&2
  usage >&2
  exit 2
fi

if [ "$variant" != "debug" ]; then
  echo "Only --variant debug is supported right now (got: $variant)" >&2
  exit 2
fi

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

marker="$(mktemp)"
cleanup() {
  rm -f "$marker"
}
trap cleanup EXIT
touch "$marker"

if [ "$skip_gradle" != "true" ]; then
  ./gradlew :app:updateDebugScreenshotTest --tests "$tests_filter"
fi

ref_dir="$repo_root/app/src/screenshotTestDebug/reference"
if [ ! -d "$ref_dir" ]; then
  echo "Reference screenshot directory not found: $ref_dir" >&2
  echo "Did the screenshot task run successfully and generate any screenshots?" >&2
  exit 1
fi

timestamp="$(date +%Y-%m-%d_%H%M%S)"
out_dir="$repo_root/$out_base/$timestamp"
mkdir -p "$out_dir"

png_files=()
while IFS= read -r file; do
  [ -n "$file" ] || continue
  png_files+=("$file")
done < <(find "$ref_dir" -type f -name '*.png' -newer "$marker" 2>/dev/null | sort)

if [ "${#png_files[@]}" -eq 0 ]; then
  filename_glob="$tests_filter"
  case "$filename_glob" in
    *\**)
      ;;
    *)
      filename_glob="*${filename_glob}*"
      ;;
  esac

  while IFS= read -r file; do
    [ -n "$file" ] || continue
    png_files+=("$file")
  done < <(find "$ref_dir" -type f -path "*${filename_glob}*.png" 2>/dev/null | sort)
fi

if [ "${#png_files[@]}" -eq 0 ]; then
  echo "No screenshots matched for export." >&2
  echo "Tip: use a broader --tests pattern matching the preview function name (e.g. '*AppTopBar*')." >&2
  exit 1
fi

for file in "${png_files[@]}"; do
  rel="${file#"$ref_dir"/}"
  dest="$out_dir/$rel"
  mkdir -p "$(dirname "$dest")"
  cp "$file" "$dest"
done

echo "Exported ${#png_files[@]} screenshot(s) to:"
echo "$out_dir"
