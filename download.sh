#!/bin/bash
UA="Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36"

baixou() {
  head -c 2 "$1" 2>/dev/null | grep -q "PK" && [ "$(stat -c%s "$1" 2>/dev/null || echo 0)" -gt 1000000 ]
}
ok() { baixou app.bin && { mv app.bin cittamobi.apk; echo "BAIXADO ($2): $(stat -c%s cittamobi.apk) bytes via $1"; exit 0; }; }

echo "== 1. Uptodown =="
for slug in cittamobi.br cittamobi.en; do
  curl -sL -A "$UA" --max-time 150 -o app.bin "https://$slug.uptodown.com/android/download" || continue
  ok "uptodown/$slug" "uptodown"
done

echo "== 2. Aptoide (API JSON) =="
search=$(curl -sL -A "$UA" --max-time 40 "https://ws75.aptoide.com/api/7/apps/search?query=cittamobi&limit=3")
echo "$search" | head -c 400 > /dev/null
path=$(echo "$search" | python3 -c "import sys,json; d=json.load(sys.stdin); l=d.get('datalist',{}).get('list',[]); print(l[0]['file']['path'] if l else '')" 2>/dev/null)
host=$(echo "$search" | python3 -c "import sys,json; d=json.load(sys.stdin); l=d.get('datalist',{}).get('list',[]); print(l[0]['file'].get('host','pool.apk.aptoide.com') if l else '')" 2>/dev/null)
echo "aptoide path: $path host: $host"
if [ -n "$path" ]; then
  for base in "https://pool.apk.aptoide.com" "https://$host"; do
    curl -sL -A "$UA" --max-time 150 -o app.bin "$base$path" || continue
    ok "aptoide" "aptoide"
  done
fi

echo "== 3. Malavida =="
page=$(curl -sL -A "$UA" --max-time 40 "https://www.malavida.com/br/android/cittamobi")
url=$(echo "$page" | grep -oE 'https://d[0-9]*\.malavida\.com/[^"\x27 ]+\.apk' | head -1)
echo "malavida: $url"
if [ -n "$url" ]; then
  curl -sL -A "$UA" --max-time 150 -o app.bin "$url"
  ok "malavida" "malavida"
fi

echo "== 4. APKFab =="
page=$(curl -sL -A "$UA" --max-time 40 "https://apkfab.com/search?q=cittamobi")
link=$(echo "$page" | grep -oE '/[a-zA-Z0-9.]+/download-apk' | head -1)
echo "apkfab: $link"
if [ -n "$link" ]; then
  page2=$(curl -sL -A "$UA" --max-time 40 "https://apkfab.com$link")
  url=$(echo "$page2" | grep -oE 'https://[a-z0-9.-]+/[a-zA-Z0-9/._-]+\.apk[^"\x27 ]*' | head -1)
  if [ -n "$url" ]; then curl -sL -A "$UA" --max-time 150 -o app.bin "$url"; ok "apkfab" "apkfab"; fi
fi

echo "FALHOU em todos os espelhos"
exit 1

# gatilho
