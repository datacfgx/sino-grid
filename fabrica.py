#!/usr/bin/env python3
# ENI FABRICA — intake via Telegram (partes <nome>_cm_NN + modo) + pipeline completo de APK
# Protocolo: 1) texto "modo <base> repack|privacy" (opcional, default repack)
#            2) partes nomeadas <base>_cm_00..NN como documento
#            3) texto "<base> fim" => remonta e forja
import json, os, re, glob, struct, subprocess, sys, time, urllib.request, urllib.parse

TOKEN = None
API = None
NL = chr(10)

def api(method, **params):
    data = urllib.parse.urlencode(params).encode()
    req = urllib.request.Request(f"{API}/{method}", data=data)
    with urllib.request.urlopen(req, timeout=180) as r:
        return json.loads(r.read().decode())

def apaga(chat, mid):
    try: api("deleteMessage", chat_id=chat, message_id=mid)
    except Exception: pass

def avisa(chat, texto):
    try: api("sendMessage", chat_id=chat, text=texto)
    except Exception as e: print("aviso falhou:", str(e)[:60])

# ---------- pipeline ----------
STORED_EXTS = (".gif", ".jpg", ".jpeg", ".png", ".webp", ".prof", ".profm", ".tflite")

def sh(cmd, timeout=600):
    r = subprocess.run(cmd, shell=True, capture_output=True, text=True, timeout=timeout)
    return r.returncode, (r.stdout + r.stderr)

def _pool(data):
    off = 8
    while off < len(data) - 8:
        ct, hs, cs = struct.unpack_from("<HHI", data, off)
        if ct == 1 and cs > 0:
            cnt, stc, fl, ss, _ = struct.unpack_from("<IIIII", data, off + 8)
            utf8 = bool(fl & 0x100)
            offs = struct.unpack_from(f"<{cnt}I", data, off + 28)
            base = off + ss
            out = []
            for o in offs:
                p = base + o
                if utf8:
                    ln = data[p]; p += 1
                    if ln & 0x80: ln = ((ln & 0x7f) << 8) | data[p]; p += 1
                    out.append(data[p:p+ln].decode(errors="replace"))
                else:
                    ln = struct.unpack_from("<H", data, p)[0]; p += 2
                    if ln & 0x8000:
                        ln = ((ln & 0x7fff) << 16) | struct.unpack_from("<H", data, p)[0]; p += 2
                    out.append(data[p:p+ln*2].decode("utf-16-le", errors="replace"))
            return out
        off += cs if cs > 0 else 8
    raise RuntimeError("string pool nao achado")

def patch_manifest(work):
    path = os.path.join(work, "AndroidManifest.xml")
    data = bytearray(open(path, "rb").read())
    pool = _pool(data)
    vazio = next(i for i, t in enumerate(pool) if t == "")
    patches = 0
    off = 8
    while off < len(data) - 8:
        ct, hs, cs = struct.unpack_from("<HHI", data, off)
        if cs <= 0: break
        if ct == 0x0102:
            ni = struct.unpack_from("<I", data, off + 20)[0]
            ast = struct.unpack_from("<H", data, off + 24)[0]
            asz = struct.unpack_from("<H", data, off + 26)[0]
            acn = struct.unpack_from("<H", data, off + 28)[0]
            if ni >= len(pool): break
            tag = pool[ni]
            attrs = []
            ba = off + 16 + ast
            for a in range(acn):
                ao = ba + a * asz
                an = struct.unpack_from("<I", data, ao + 4)[0]
                av = struct.unpack_from("<I", data, ao + 16)[0]
                if an < len(pool): attrs.append((ao, pool[an], av))
            alvos = {"requiredSplitTypes", "isSplitRequired"}
            if tag == "manifest":
                for ao, an, av in attrs:
                    if an in alvos:
                        struct.pack_into("<I", data, ao + 8, vazio)
                        struct.pack_into("<I", data, ao + 16, vazio)
                        patches += 1
            if tag == "meta-data":
                nm = next((av for ao, an, av in attrs if an == "name"), None)
                if nm is not None and nm < len(pool) and "splits.required" in pool[nm]:
                    for ao, an, av in attrs:
                        if an in ("value", "resource"):
                            struct.pack_into("<I", data, ao + 8, vazio)
                            struct.pack_into("<I", data, ao + 16, vazio)
                            patches += 1
        off += cs
    open(path, "wb").write(bytes(data))
    return patches

def regra_stored(rel):
    return rel == "resources.arsc" or rel.startswith("lib/") or rel.endswith(STORED_EXTS)

def extrai(arq, dest):
    import zipfile
    os.makedirs(dest, exist_ok=True)
    with zipfile.ZipFile(arq) as z:
        z.extractall(dest)

def zipa(work, saida):
    import zipfile
    with zipfile.ZipFile(saida, "w") as z:
        for root, _, fs in os.walk(work):
            for f in fs:
                full = os.path.join(root, f)
                rel = os.path.relpath(full, work)
                z.write(full, rel, compress_type=zipfile.ZIP_STORED if regra_stored(rel) else zipfile.ZIP_DEFLATED)

def normaliza(entrada, saida):
    """XAPK/split -> universal (ou repassa APK com manifest remendado + compressao corrigida)"""
    import zipfile
    tmp = "fab_work"
    subprocess.run(f"rm -rf {tmp} merged", shell=True)
    os.makedirs(tmp, exist_ok=True)
    with zipfile.ZipFile(entrada) as z:
        nomes = z.namelist()
    eh_xapk = any(n.endswith(".apk") for n in nomes)
    if eh_xapk:
        with zipfile.ZipFile(entrada) as z:
            for n in nomes:
                if n.endswith(".apk"):
                    z.extract(n, tmp)
        os.makedirs("merged", exist_ok=True)
        base = None
        for root, _, fs in os.walk(tmp):
            for f in fs:
                p = os.path.join(root, f)
                with zipfile.ZipFile(p) as z:
                    tem_manifest = "AndroidManifest.xml" in z.namelist()
                    tem_lib = any(x.startswith("lib/") for x in z.namelist())
                if tem_manifest and base is None:
                    extrai(p, "merged")
                    base = True
                elif tem_lib:
                    with zipfile.ZipFile(p) as z:
                        for i in z.infolist():
                            if i.filename.startswith("lib/"): z.extract(i, "merged")
        alvo = "merged"
    else:
        extrai(entrada, "merged")
        alvo = "merged"
    n = patch_manifest(alvo)
    print(f"manifest remendado: {n} atributo(s)")
    zipa(alvo, saida)
    print(f"universal: {os.path.getsize(saida)} bytes")
    return n

def ferramentas():
    if not os.path.exists("bt/android-14/lib/apksigner.jar"):
        sh("wget -q https://dl.google.com/android/repository/build-tools_r34-linux.zip && unzip -q build-tools_r34-linux.zip -d bt")
    if not os.path.exists("bt/android-14/zipalign"):
        raise RuntimeError("build-tools incompleto: bt/android-14/zipalign ausente")
    if os.path.exists("eni.keystore.b64"):
        sh("base64 -d eni.keystore.b64 > eni.keystore")
    if not os.path.exists("eni.keystore"):
        raise RuntimeError("keystore indisponivel: eni.keystore.b64 ausente na branch")

def poison(path_apk):
    SUBS = [
        (b"volceapplog.com", b"127.0.0.1.xxxxx"),
        (b"volctrack.com",   b"127.0.0.1.xxx"),
        (b"crash.163.com",   b"127.0.0.1.cra"),
    ]
    import zipfile, shutil
    tmp = "poi_work"
    subprocess.run(f"rm -rf {tmp}", shell=True)
    with zipfile.ZipFile(path_apk) as z: z.extractall(tmp)
    total = 0
    for root, _, fs in os.walk(tmp):
        for f in fs:
            if not f.endswith(".dex"): continue
            p = os.path.join(root, f)
            data = open(p, "rb").read()
            n = 0
            for old, new in SUBS:
                assert len(old) == len(new)
                c = data.count(old)
                if c: data = data.replace(old, new); n += c
            if n: open(p, "wb").write(data); total += n
    zipa(tmp, "poisoned.apk")
    print(f"poison: {total} substituicoes")
    return total

def assina_e_verifica(apk, saida="final.apk"):
    ferramentas()
    env = "LD_LIBRARY_PATH=$PWD/bt/android-14/lib64"
    rc, out = sh(f"{env} bt/android-14/zipalign -f -p 4 {apk} aligned.apk")
    if rc != 0:
        raise RuntimeError("zipalign falhou: " + out[-160:])
    rc, out = sh(f"java -jar bt/android-14/lib/apksigner.jar sign --ks eni.keystore --ks-pass pass:eni123456 --key-pass pass:eni123456 --ks-key-alias eni --out {saida} aligned.apk")
    if rc != 0:
        raise RuntimeError("apksigner falhou: " + out[-160:])
    checks = []
    rc, _ = sh(f"{env} bt/android-14/zipalign -c -p 4 {saida}")
    checks.append(("alinhamento", rc == 0))
    rc, out = sh(f"java -jar bt/android-14/lib/apksigner.jar verify --verbose {saida}")
    checks.append(("assinatura v1+v2", "Verifies" in out and "true" in out))
    import zipfile
    z = zipfile.ZipFile(saida)
    nomes = z.namelist()
    arsc = [i for i in z.infolist() if i.filename == "resources.arsc"]
    checks.append(("arsc STORED", bool(arsc) and arsc[0].compress_type == 0))
    libs_ruins = [i.filename for i in z.infolist() if i.filename.endswith(".so") and i.compress_type != 0]
    checks.append(("libs STORED", not libs_ruins))
    man = z.read("AndroidManifest.xml")
    checks.append(("splitreq morto", b"base__abi" not in man and b"base__density" not in man))
    ztest = z.testzip()
    checks.append(("zip intacto", ztest is None))
    for nome, ok in checks:
        print(("OK " if ok else "FALHOU ") + nome)
    return all(ok for _, ok in checks), checks

def entrega(chat, caminho, base):
    tam = os.path.getsize(caminho)
    import math
    n = 1 if tam <= 20_000_000 else math.ceil(tam / 18_000_000)
    if n == 1:
        for i in range(3):
            try:
                api("sendDocument", chat_id=chat, document=open(caminho, "rb"), caption=f"{base} forjado — {tam} bytes")
                return True
            except Exception: time.sleep(8)
    else:
        subprocess.run(f"split -b 18000000 -d -a 2 {caminho} fatia_", shell=True)
        for f in sorted(glob.glob("fatia_*")):
            for i in range(3):
                try:
                    api("sendDocument", chat_id=chat, document=open(f, "rb"), caption=f)
                    break
                except Exception: time.sleep(8)
        avisa(chat, f"{base} em {n} fatias — junta no termux: cat fatia_* > {base}-forjado.apk")
    return True


def procura(termo, destino):
    """busca o APK na internet (espelhos, parse de pagina) e baixa pra destino"""
    UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36"
    def get(url, timeout=40):
        req = urllib.request.Request(url, headers={"User-Agent": UA})
        with urllib.request.urlopen(req, timeout=timeout) as r:
            return r.read()
    def eh_apk(data):
        return len(data) > 500_000 and data[:2] == b"PK"
    # 1) uptodown slugs
    slug = termo.lower().replace(" ", "-")
    for s2 in [f"{slug}.br", f"{slug}.en"]:
        try:
            d = get(f"https://{s2}.uptodown.com/android/download", 60)
            if eh_apk(d):
                open(destino, "wb").write(d)
                print(f"uptodown/{s2}: {len(d)//1024//1024}MB")
                return True
        except Exception:
            pass
    # 2) apkcombo parse
    try:
        page = get(f"https://apkcombo.com/pt-br/search/{urllib.parse.quote(termo)}", 40).decode(errors="replace")
        m = re.search(r'/([a-zA-Z0-9.]+)/download/phone', page)
        if m:
            page2 = get(f"https://apkcombo.com/{m.group(1)}/download/phone", 40).decode(errors="replace")
            v = re.search(r'(/[a-zA-Z0-9.]+/download/apk\?variant=[a-zA-Z0-9._-]+)', page2)
            if v:
                d = get("https://apkcombo.com" + v.group(1), 180)
                if eh_apk(d):
                    open(destino, "wb").write(d)
                    print(f"apkcombo: {len(d)//1024//1024}MB")
                    return True
    except Exception as e:
        print("apkcombo:", str(e)[:60])
    return False

# ---------- intake ----------
def main():
    global TOKEN, API
    TOKEN = open("correio_token.txt").read().strip()
    API = f"https://api.telegram.org/bot{TOKEN}"
    try:
        upd = api("getUpdates", timeout=60, limit=100)
    except Exception:
        api("deleteWebhook", drop_pending_updates="false")
        upd = api("getUpdates", timeout=60, limit=100)

    os.makedirs("parts", exist_ok=True)
    modos = {}
    baixou = 0
    fila_fim = []
    max_id = 0
    for u in upd.get("result", []):
        max_id = max(max_id, u.get("update_id", 0))
        msg = u.get("message") or {}
        chat = msg.get("chat", {}).get("id")
        doc = msg.get("document")
        texto = (msg.get("text") or "").strip().lower()
        if doc:
            m = re.fullmatch(r"([a-z0-9_\-]{2,40})_cm_\d{2,4}", doc.get("file_name", ""))
            if m:
                nome = doc["file_name"]
                info = api("getFile", file_id=doc["file_id"]).get("result", {})
                url = f"https://api.telegram.org/file/bot{TOKEN}/{info.get('file_path','')}"
                urllib.request.urlretrieve(url, f"parts/{nome}")
                apaga(chat, msg["message_id"])
                baixou += 1
                print(f"parte {nome}: {os.path.getsize(f'parts/{nome}')} bytes")
        elif texto.startswith("modo "):
            partes = texto.split(None, 2)
            if len(partes) >= 3:
                modos[partes[1]] = partes[2]
                apaga(chat, msg["message_id"])
        elif texto.startswith("procura "):
            termo = texto[8:].strip()
            alvo = re.sub(r"[^a-z0-9]+", "_", termo.lower())[:30]
            avisa(chat, f"🔎 APK TUNING MOD procurando '{termo}' na internet...")
            try:
                if procura(termo, f"{alvo}.pacote"):
                    tam = os.path.getsize(f"{alvo}.pacote")
                    avisa(chat, f"📥 achei: {alvo}.pacote ({tam} bytes). Manda '{alvo} fim' pra forjar (ou 'modo {alvo} privacy' antes).")
                else:
                    avisa(chat, f"🔎 nao achei '{termo}' nos espelhos. Manda o APK pelo correio: split -b 18000000 -d -a 2 arquivo.apk {alvo}_cm_")
            except Exception as e:
                avisa(chat, f"🔎 busca falhou: {str(e)[:120]}")
            apaga(chat, msg["message_id"])
        elif texto.endswith(" fim"):
            base = texto.split()[0]
            fila_fim.append((base, chat))
            apaga(chat, msg["message_id"])

    print(f"partes: {baixou} | fins: {[b for b,_ in fila_fim]} | modos: {modos}")

    for base, chat in fila_fim:
        partes = sorted(glob.glob(f"parts/{base}_cm_*"),
                        key=lambda p: int(re.search(r"_cm_(\d+)", p).group(1)))
        if not partes:
            avisa(chat, f"📦 {base}: recebi 'fim' mas nao achei partes {base}_cm_*")
            continue
        bruto = f"{base}.pacote"
        with open(bruto, "wb") as o:
            for p in partes:
                o.write(open(p, "rb").read())
        cab = open(bruto, "rb").read(2)
        avisa(chat, f"🏭 APK TUNING MOD: {base} remontado ({os.path.getsize(bruto)} bytes, zip={'OK' if cab==b'PK' else '??'}) — forjando...")
        try:
            n = normaliza(bruto, "universal.apk")
            modo = modos.get(base, "repack")
            alvo = "universal.apk"
            if modo == "privacy":
                poison(alvo)
                alvo = "poisoned.apk"
            ok, checks = assina_e_verifica(alvo)
            if ok:
                entrega(chat, "final.apk", base)
                avisa(chat, f"✅ {base} FORJADO e verificado ({modo}). Instala e me conta.")
            else:
                falhas = [n for n, ok2 in checks if not ok2]
                avisa(chat, f"⚠️ {base}: falhou na verificacao: {', '.join(falhas)} — verificando logs da run")
            for p in partes: os.remove(p)
        except Exception as e:
            avisa(chat, f"❌ {base} quebrou na forja: {str(e)[:180]}")
            import traceback; traceback.print_exc()

    try:
        if max_id:
            api("getUpdates", offset=max_id + 1, timeout=0)
            print(f"offset confirmado: {max_id + 1}")
    except Exception as e:
        print("offset nao confirmado:", str(e)[:80])

if __name__ == "__main__":
    main()
