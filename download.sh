#!/bin/bash
# baixa o APK do Cittamobi: parse real do APKCombo (pagina -> variante -> arquivo assinado)
UA="Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36"

baixou() {
  head -c 2 "$1" 2>/dev/null | grep -q "PK" && [ "$(stat -c%s "$1")" -gt 1000000 ]
}

echo "== 1. buscando pacote no APKCombo =="
page=$(curl -sL -A "$UA" --max-time 60 "https://apkcombo.com/pt-br/search/cittamobi")
path=$(echo "$page" | grep -oE '/[a-zA-Z0-9.]+/download/phone' | head -1)
echo "caminho: $path"
if [ -n "$path" ]; then
  page2=$(curl -sL -A "$UA" --max-time 60 "https://apkcombo.com$path")
  link=$(echo "$page2" | grep -oE '/[a-zA-Z0-9.]+/download/apk\?variant=[a-zA-Z0-9._-]+' | head -1)
  echo "variante: $link"
  if [ -n "$link" ]; then
    curl -sL -A "$UA" --max-time 180 -o app.bin "https://apkcombo.com$link"
    if baixou app.bin; then mv app.bin cittamobi.apk; echo "BAIXADO via APKCombo: $(stat -c%s cittamobi.apk) bytes"; exit 0; fi
  fi
fi

echo "== 2. fallback: tentativas diretas =="
for pkg in com.cittamobi.app com.cittamobi; do
  for url in "https://d.apkpure.com/b/APK/$pkg" "https://apkcombo.com/$pkg/download/apk"; do
    curl -sL -A "$UA" --max-time 120 -o app.bin "$url" || continue
    if baixou app.bin; then mv app.bin cittamobi.apk; echo "BAIXADO via $url: $(stat -c%s cittamobi.apk) bytes"; exit 0; fi
  done
done
echo "FALHOU"
exit 1
