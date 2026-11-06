#!/bin/bash
# baixa o APK do Cittamobi de mirrors (IP limpo do runner)
UA="Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 Chrome/124 Mobile Safari/537.36"
for pkg in com.cittamobi.app com.cittamobi; do
  for url in "https://d.apkpure.com/b/APK/$pkg" "https://apkcombo.com/$pkg/download/apk"; do
    echo "tentando: $url"
    curl -sL -A "$UA" --max-time 150 -o app.bin "$url" || continue
    head -c 2 app.bin | grep -q "PK" || { echo "  (nao e APK)"; continue; }
    sz=$(stat -c%s app.bin)
    [ "$sz" -gt 1000000 ] || { echo "  (muito pequeno: $sz)"; continue; }
    mv app.bin cittamobi.apk
    echo "BAIXADO: cittamobi.apk ($sz bytes) via $url"
    exit 0
  done
done
echo "FALHOU: nenhum mirror entregou o APK"
exit 1
