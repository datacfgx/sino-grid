#!/usr/bin/env python3
# monta_tez.py — ENI Mod Tezza: assemble do dex do veredito + montagem do APK universal + entrega
# roda no runner (disco de sobra). Recebe as partes via download-artifact (step do yml) ou baixa sozinho.
import os, sys, subprocess, urllib.request, json, glob, shutil, zipfile, re, time

BANC = os.getcwd()
CHAT = os.environ.get("TELEGRAM_CHAT_ID", "")

def sh(c, check=True):
    r = subprocess.run(c, shell=True, capture_output=True, text=True)
    if check and r.returncode != 0:
        raise RuntimeError(f"{c[:60]} rc={r.returncode}: {r.stderr[-160:]}")
    return r.returncode, r.stdout + r.stderr

def baixa(url, dest, tok=None):
    req = urllib.request.Request(url, headers={"Authorization": f"Bearer {tok}"} if tok else {})
    with urllib.request.urlopen(req, timeout=300) as r, open(dest, "wb") as f:
        shutil.copyfileobj(r, f, 1024*1024)

# ---------- 1) assemble do classes2.dex com o patch ----------
print("== assemble do dex do veredito")
sh("unzip -q -o smali_tez2.zip -d smali_tez2")
for jar in ["smali.jar", "util.jar", "jcommander.jar"]:
    baixa(f"https://dl.google.com/dl/android/maven2/com/android/tools/smali/{'util' if jar=='util.jar' else 'smali'}/3.0.9/{'util' if jar=='util.jar' else 'smali'}-3.0.9.jar" if jar != "jcommander.jar" else
          "https://repo1.maven.org/maven2/com/beust/jcommander/1.85/jcommander-1.85.jar", jar)
sh("java -cp smali.jar:util.jar:jcommander.jar com.android.tools.smali.smali.Main assemble smali_tez2/smali -o classes2_new.dex")
data = open("classes2_new.dex", "rb").read()
assert data[:4] == b"dex\n", "dex invalido"
print("classes2_new.dex ok:", len(data))

# ---------- 2) partes do deposito ----------
print("== remontando o XAPK")
if not os.path.isdir("deposito") or not glob.glob("deposito/tezza_cm_*"):
    tok = os.environ.get("WORKFLOW_PAT") or os.environ.get("GH_TOKEN")
    arts = json.loads(urllib.request.urlopen(urllib.request.Request(
        f"https://api.github.com/repos/{os.environ.get('GITHUB_REPOSITORY')}/actions/artifacts?per_page=50",
        headers={"Authorization": f"Bearer {tok}", "Accept": "application/vnd.github+json"}), timeout=60).read())
    alvo = next(a for a in arts["artifacts"] if a["name"] == "deposito" and a["size_in_bytes"] > 300_000_000)
    baixa(alvo["archive_download_url"], "deposito.zip", tok)
    zipfile.ZipFile("deposito.zip").extractall("deposito")
partes = sorted(glob.glob("deposito/tezza_cm_*"))
assert len(partes) >= 20, f"so {len(partes)} partes"
with open("tezza.xapk", "wb") as o:
    for p in partes:
        o.write(open(p, "rb").read())
print("xapk remontado:", os.path.getsize("tezza.xapk"))

# ---------- 3) base + dex novo + fusao arm64 ----------
print("== montando o universal")
zx = zipfile.ZipFile("tezza.xapk")
shutil.rmtree("base_work", ignore_errors=True)
zx.extract("org.tezza.apk", ".")
zipfile.ZipFile("org.tezza.apk").extractall("base_work")
for d in ["classes2.dex", "classes2_new.dex"]:
    if os.path.exists(f"base_work/{d}"):
        os.remove(f"base_work/{d}")
shutil.copy("classes2_new.dex", "base_work/classes2.dex")
print("dex do veredito injetado")
# libs arm64
zx.extract("config.arm64_v8a.apk", ".")
zipfile.ZipFile("config.arm64_v8a.apk").extractall("arm64_work")
for raiz, _, files in os.walk("arm64_work"):
    for f in files:
        if f.endswith(".so"):
            rel = os.path.relpath(os.path.join(raiz, f), "arm64_work")
            dst = os.path.join("base_work", rel)
            os.makedirs(os.path.dirname(dst), exist_ok=True)
            shutil.copy2(os.path.join(raiz, f), dst)
print("libs arm64 fundidas")

# ---------- 4) manifest sem split-types + rezip STORED ----------
def patch_manifest(work):
    p = os.path.join(work, "AndroidManifest.xml")
    data = bytearray(open(p, "rb").read())
    # zero requiredSplitTypes/isSplitRequired: troca strings no pool por "" — metodo da casa
    import struct
    (hsize,) = struct.unpack_from("<H", data, 2)
    (sc, ssty, flags, sstart, _) = struct.unpack_from("<IIIII", data, hsize)
    # acha string pool e substitui as strings alvo por vazias (best effort: so funciona se pool UTF8 e string existir)
    # abordagem segura: procura bytes das strings e encurta NAO e trivial; usa abordagem do fabrica (pool edit)
    off = hsize
    for _ in range(40):
        if off + 8 > len(data): break
        t, hs, sz = struct.unpack_from("<HHI", data, off)
        if t == 0x0001:
            n, _, fl, ss, _ = struct.unpack_from("<IIIII", data, off + 8)
            base = off + 28
            offs = [struct.unpack_from("<I", data, base + 4*i)[0] for i in range(n)]
            strs = []
            for so in offs:
                q = off + ss + so
                l1, l2 = data[q], data[q+1]
                strs.append(bytes(data[q+2:q+2+l2]))
            for i, s in enumerate(strs):
                if s in (b"requiredSplitTypes", b"isSplitRequired"):
                    data[off+ss+offs[i]:off+ss+offs[i]+len(s)] = b"\x00" * len(s)
                    print(f"manifest remendado: {s.decode()}")
            break
        off += sz
    open(p, "wb").write(bytes(data))
patch_manifest("base_work")

def regra(rel):
    return rel == "resources.arsc" or rel.startswith("lib/") or rel.endswith(".so")

zin = zipfile.ZipFile("org.tezza.apk")
zout = zipfile.ZipFile("base_mod.apk", "w", zipfile.ZIP_DEFLATED)
for item in zin.infolist():
    if item.filename.endswith(".dex"):
        continue
    if item.filename.startswith("lib/"):
        continue
    zi = zipfile.ZipInfo(item.filename, date_time=item.date_time)
    zi.compress_type = zipfile.ZIP_STORED if regra(item.filename) else zipfile.ZIP_DEFLATED
    zout.writestr(zi, open(os.path.join("base_work", item.filename), "rb").read() if os.path.exists(os.path.join("base_work", item.filename)) else zin.read(item.filename))
for raiz, _, files in os.walk("base_work"):
    for f in files:
        rel = os.path.relpath(os.path.join(raiz, f), "base_work")
        if rel.endswith(".dex") or rel in [i.filename for i in zin.infolist()]:
            if not rel.endswith(".dex"):
                continue
for d in ["classes.dex", "classes2.dex"]:
    zi = zipfile.ZipInfo(d)
    zi.compress_type = zipfile.ZIP_STORED
    zout.writestr(zi, open(f"base_work/{d}", "rb").read())
zout.close()
print("base_mod.apk:", os.path.getsize("base_mod.apk"))

# ---------- 5) assinatura ----------
sh("wget -q https://dl.google.com/android/repository/build-tools_r34-linux.zip && unzip -q build-tools_r34-linux.zip -d bt")
sh("base64 -d eni.keystore.b64 > eni.keystore")
env = 'LD_LIBRARY_PATH=$PWD/bt/android-14/lib64'
sh(f"{env} bt/android-14/zipalign -f -p 4 base_mod.apk alinhado.apk")
sh(f"java -jar bt/android-14/lib/apksigner.jar sign --ks eni.keystore --ks-pass pass:eni123456 --key-pass pass:eni123456 --ks-key-alias eni --out tezza-mod.apk alinhado.apk")
rc, out = sh("java -jar bt/android-14/lib/apksigner.jar verify --print-certs tezza-mod.apk")
assert rc == 0, "assinatura falhou"
print("assinado v1+v2:", os.path.getsize("tezza-mod.apk"), "bytes")

# ---------- 6) entrega telegram (fatiado) ----------
TOK = os.environ["TELEGRAM_TOKEN"]
def envia(caminho, legenda):
    for i in range(3):
        r = subprocess.run(["curl", "-s", "-F", f"chat_id={CHAT}", "-F", f"document=@{caminho}",
                            "-F", f"caption={legenda}", f"https://api.telegram.org/bot{TOK}/sendDocument"],
                           capture_output=True, text=True, timeout=600)
        if r.returncode == 0 and '"ok":true' in r.stdout:
            return True
        time.sleep(8)
    return False

tam = os.path.getsize("tezza-mod.apk")
if tam <= 20_000_000:
    envia("tezza-mod.apk", f"Tezza mod — {tam} bytes (assinado ENI)")
else:
    subprocess.run("split -b 18000000 -d -a 2 tezza-mod.apk tezza_out_", shell=True, check=True)
    for f in sorted(glob.glob("tezza_out_*")):
        assert envia(f, f), f"fatia {f} falhou"
    req = urllib.request.Request(f"https://api.telegram.org/bot{TOK}/sendMessage",
        data=json.dumps({"chat_id": CHAT, "text": f"Tezza mod em fatias — junta: cat tezza_out_* > tezza-mod.apk ({tam} bytes)"}).encode(),
        headers={"Content-Type": "application/json"})
    urllib.request.urlopen(req, timeout=60)
print("ENTREGUE no telegram")

# ---------- 7) tentativa de release (bonus) ----------
try:
    tok = os.environ.get("WORKFLOW_PAT") or os.environ.get("GH_TOKEN")
    req = urllib.request.Request(f"https://api.github.com/repos/{os.environ['GITHUB_REPOSITORY']}/releases",
        data=json.dumps({"tag_name": "tezza-mod-v1", "name": "Tezza Mod v1 — todos os filtros liberados", "body": "APK universal assinado ENI. Todos os filtros Premium destravados."}).encode(),
        headers={"Authorization": f"Bearer {tok}", "Accept": "application/vnd.github+json"})
    rel = json.loads(urllib.request.urlopen(req, timeout=60).read())
    up = rel["upload_url"].split("{")[0] + "?name=Tezza-mod.apk"
    req = urllib.request.Request(up, data=open("tezza-mod.apk", "rb").read(),
        headers={"Authorization": f"Bearer {tok}", "Content-Type": "application/vnd.android.package-archive", "Content-Length": str(tam)})
    urllib.request.urlopen(req, timeout=900)
    print("RELEASE PUBLICADO")
except Exception as e:
    print("release nao foi (telegram ja entregou):", str(e)[:120])
print("FIM DO TRAMPO")
