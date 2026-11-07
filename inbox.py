#!/usr/bin/env python3
# ENI Inbox v3.1 — correio via Telegram (partes cm_XX + cmfim)
import json, os, re, glob, urllib.request, urllib.parse

TOKEN = open("correio_token.txt").read().strip()
API = f"https://api.telegram.org/bot{TOKEN}"
NL = chr(10)
AJUDA = ("📦 Correio ENI: envio grande precisa ser FATIADO." + NL + NL +
         "No Termux:" + NL +
         '  split -b 18000000 -d -a 2 "<arquivo>" cm_' + NL +
         "Depois mande cm_00, cm_01... como ARQUIVO (um por vez)" + NL +
         "e ao final a palavra: cmfim" + NL +
         "(arquivo unico acima de 20MB eu nao consigo baixar — limite do Telegram pra bots)")

def api(method, **params):
    data = urllib.parse.urlencode(params).encode()
    req = urllib.request.Request(f"{API}/{method}", data=data)
    with urllib.request.urlopen(req, timeout=120) as r:
        return json.loads(r.read().decode())

def apaga(chat, mid):
    try:
        api("deleteMessage", chat_id=chat, message_id=mid)
    except Exception as e:
        print("del falhou:", str(e)[:60])

try:
    upd = api("getUpdates", timeout=60, limit=100)
except Exception:
    api("deleteWebhook", drop_pending_updates="false")
    upd = api("getUpdates", timeout=60, limit=100)

os.makedirs("parts", exist_ok=True)
baixou, fim, chat_fim = 0, False, None
for u in upd.get("result", []):
    msg = u.get("message") or {}
    doc = msg.get("document")
    chat = msg.get("chat", {}).get("id")
    if doc:
        name = doc.get("file_name", "")
        if re.fullmatch(r"cm_\d{2,4}", name):
            info = api("getFile", file_id=doc["file_id"]).get("result", {})
            url = f"https://api.telegram.org/file/bot{TOKEN}/{info.get('file_path','')}"
            urllib.request.urlretrieve(url, f"parts/{name}")
            apaga(chat, msg["message_id"])
            baixou += 1
            print(f"parte {name}: {os.path.getsize(f'parts/{name}')} bytes")
        elif doc.get("file_size", 0) > 19000000:
            api("sendMessage", chat_id=chat, text=AJUDA,
                reply_to_message_id=msg.get("message_id"))
            print(f"aviso enviado pra: {name}")
    elif (msg.get("text") or "").strip().lower() == "cmfim":
        fim, chat_fim = True, chat
        apaga(chat, msg["message_id"])

print(f"partes novas: {baixou} | fim: {fim}")
partes = sorted(glob.glob("parts/cm_*"), key=lambda p: int(re.search(r"cm_(\d+)", p).group(1)))
print("acumuladas:", [os.path.basename(p) for p in partes])

if fim and partes:
    out = "correio.bin"
    with open(out, "wb") as o:
        for p in partes:
            o.write(open(p, "rb").read())
    tam = os.path.getsize(out)
    ok = open(out, "rb").read(2) == b"PK"
    api("sendMessage", chat_id=chat_fim,
        text=f"📦 Correio remontado: {len(partes)} partes = {tam} bytes (zip={'OK' if ok else 'ATENCAO'})")
    print(f"RECEBIDO {tam} bytes zip={ok}")
    for p in partes:
        os.remove(p)
elif fim:
    api("sendMessage", chat_id=chat_fim, text="📦 cmfim recebido mas sem partes cm_* — reenvia as partes")
