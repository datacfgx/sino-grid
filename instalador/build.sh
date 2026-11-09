#!/bin/sh
# build.sh — forja o ENI Instalador XAPK sem Gradle
set -e
cd "$(dirname "$0")"
BT=bt
mkdir -p build
echo "== aapt2 link =="
$BT/aapt2 link -o build/base.apk -I android.jar --manifest AndroidManifest.xml \
    --java build/gen --min-sdk-version 24 --target-sdk-version 34
echo "== javac =="
mkdir -p build/classes
javac -source 8 -target 8 -cp android.jar -d build/classes \
    src/com/eni/instalador/MainActivity.java build/gen/com/eni/instalador/R.java
echo "== d8 (classes.jar) =="
cd build/classes && jar cf ../classes.jar . && cd ../..
java -cp $BT/lib/d8.jar com.android.tools.r8.D8 \
    --min-api 24 --lib android.jar --output build/ build/classes.jar
echo "== empacota =="
mkdir -p build/x && cd build/x && unzip -q -o ../base.apk && cp ../classes.dex . && zip -q -r ../unsigned.apk . && cd ../..
echo "== alinha + assina =="
LD_LIBRARY_PATH=$PWD/$BT/lib64 $BT/zipalign -f -p 4 build/unsigned.apk build/al.apk
java -jar $BT/lib/apksigner.jar sign --ks eni.keystore --ks-pass pass:eni123456 \
    --key-pass pass:eni123456 --ks-key-alias eni --out eni-instalador-xapk.apk build/al.apk
java -jar $BT/lib/apksigner.jar verify --print-certs eni-instalador-xapk.apk
echo "OK: eni-instalador-xapk.apk"
