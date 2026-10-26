#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_DIR="$DIR/app-src"
BUILD_DIR="$APP_DIR/build"
TOOLS_DIR="$HOME/.local/android-tools"

echo "==> Preparing build directories..."
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR/compiled" "$BUILD_DIR/gen" "$BUILD_DIR/classes" "$BUILD_DIR/signed"

echo "==> 1. Compiling Android resources with aapt2..."
"$TOOLS_DIR/aapt2" compile --dir "$APP_DIR/res" -o "$BUILD_DIR/compiled/res.zip"

echo "==> 2. Linking resources with aapt2..."
"$TOOLS_DIR/aapt2" link -o "$BUILD_DIR/app-unaligned.apk" \
  -I "$TOOLS_DIR/android.jar" \
  --manifest "$APP_DIR/AndroidManifest.xml" \
  --java "$BUILD_DIR/gen" \
  --auto-add-overlay \
  "$BUILD_DIR/compiled/res.zip"

echo "==> 3. Compiling Java sources with javac..."
javac -d "$BUILD_DIR/classes" \
  -cp "$TOOLS_DIR/android.jar" \
  "$BUILD_DIR/gen/com/afan/headunitserver/R.java" \
  "$APP_DIR"/src/com/afan/headunitserver/*.java

echo "==> 4. Converting bytecode to Dalvik Executable (D8)..."
cd "$BUILD_DIR/classes"
CLASS_FILES=$(find . -name "*.class")
java -cp "$TOOLS_DIR/r8.jar" com.android.tools.r8.D8 \
  --release \
  --min-api 26 \
  --lib "$TOOLS_DIR/android.jar" \
  --output "$BUILD_DIR" \
  $CLASS_FILES
cd "$DIR"

echo "==> 5. Packaging classes.dex into APK..."
cd "$BUILD_DIR"
zip -u app-unaligned.apk classes.dex
cd "$DIR"

echo "==> 6. Signing APK with uber-apk-signer (v1, v2, v3)..."
java -jar "$TOOLS_DIR/uber-apk-signer.jar" \
  -a "$BUILD_DIR/app-unaligned.apk" \
  -o "$BUILD_DIR/signed"

mkdir -p "$DIR/outputs"
cp "$BUILD_DIR/signed/app-aligned-debugSigned.apk" "$DIR/outputs/AndroidAutoServer-v1.4.0.apk"
cp "$BUILD_DIR/signed/app-aligned-debugSigned.apk" "$DIR/AndroidAutoServer.apk"

echo "==> Build successful: $DIR/outputs/AndroidAutoServer-v1.4.0.apk"
