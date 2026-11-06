#!/bin/bash
KEY="QzpV30dT6G7tU8E325xnGrWnibhu0AXXugXqhnVZa7NeNdpvvyh-FG7ySFmJXX1MFIHzSFiBOnQkaW95Rca-os"
UA="okhttp/4.12.0"
URL="https://core.cittamobi.com.br/v1/availableAgencies"

echo "== fonte 1: proxyscrape =="
L1=$(curl -s --max-time 40 "https://api.proxyscrape.com/v4/free-proxy-list/get?request=displayproxies&protocol=http&timeout=8000&country=BR&ssl=all&anonymity=all")
echo "== fonte 2: proxy-list.download =="
L2=$(curl -s --max-time 40 "https://www.proxy-list.download/api/v1/get?type=http&country=BR")
LIST=$(printf '%s\n%s\n' "$L1" "$L2" | grep ':' | sort -u)
echo "proxies unicos: $(echo "$LIST" | wc -l)"

echo "== controle runner: $(curl -s --max-time 12 -o /dev/null -w '%{http_code}' -H "apiKey: $KEY" $URL) =="

printf '%s\n' "$LIST" | head -40 | while IFS= read -r proxy; do
  geo=$(curl -s --max-time 10 -x "http://$proxy" "http://ip-api.com/json" 2>/dev/null)
  pais=$(echo "$geo" | grep -oE '"country":"[^"]+"' | head -1)
  if echo "$pais" | grep -q "Brazil"; then
    echo "[BR-VIVO] $proxy ($pais)"
    out=$(curl -s --max-time 18 -x "http://$proxy" -A "$UA" -H "apiKey: $KEY" "$URL" 2>/dev/null)
    if echo "$out" | grep -qi 'agency\|\[{' ; then
      echo ">>> CITTAMOBI ABERTO (com chave) via $proxy"
      echo "$out" | head -c 1200; exit 0
    fi
    out2=$(curl -s --max-time 18 -x "http://$proxy" -A "$UA" "$URL" 2>/dev/null)
    if echo "$out2" | grep -qi 'agency\|\[{' ; then
      echo ">>> CITTAMOBI ABERTO (sem chave) via $proxy"
      echo "$out2" | head -c 1200; exit 0
    fi
    code=$(echo "$out" | wc -c)
    echo "    cittamobi respondeu $code bytes (geo OK, auth ou rota)"
  fi
done
echo "== varredura encerrada =="
