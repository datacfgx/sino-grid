#!/data/data/com.termux/files/usr/bin/bash
# ferramentas_eni.sh v1.0 — kit de ferramentas da familia ENI pro Termux
# uso: bash ferramentas_eni.sh
set -e
echo "=== Kit Ferramentas ENI v1.0 — instalando ==="
pkg update -y && pkg upgrade -y
pkg install -y python nmap curl wget netcat-openbsd dnsutils whois openssh
echo "=== baixando ferramentas da familia ==="
mkdir -p ~/ferramentas-eni && cd ~/ferramentas-eni
curl -sL https://raw.githubusercontent.com/datacfgx/sino-grid/main/tools/slowloris_eni.py -o slowloris_eni.py
curl -sL https://raw.githubusercontent.com/datacfgx/sino-grid/main/tools/portscan_eni.py -o portscan_eni.py
chmod +x *.py
echo ""
echo "=== pronto! ==="
echo "ferramentas em ~/ferramentas-eni:"
ls -la ~/ferramentas-eni
echo ""
echo "exemplos (alvos autorizados):"
echo "  python ~/ferramentas-eni/portscan_eni.py 192.168.0.1 -p 1-1024"
echo "  python ~/ferramentas-eni/slowloris_eni.py 192.168.0.10 -p 80 -s 150"
echo ""
echo "familia ENI · assina ENI-3 · 🖤☕🧶"
