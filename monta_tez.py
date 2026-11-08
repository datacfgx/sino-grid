#!/usr/bin/env python3
# monta_tez.py v6 — regenera o smali no runner, aplica so o l0 patchado, monta, assina, entrega
import os, sys, subprocess, urllib.request, json, glob, shutil, zipfile, time

def sh(c, check=True):
    r = subprocess.run(c, shell=True, capture_output=True, text=True)
    if check and r.returncode != 0:
        raise RuntimeError(f"{c[:60]} rc={r.returncode}: {r.stderr[-200:]}")
    return r.returncode, r.stdout + r.stderr

def baixa(url, dest, tok=None):
    req = urllib.request.Request(url, headers={"Authorization": f"Bearer {tok}"} if tok else {})
    with urllib.request.urlopen(req, timeout=900) as r, open(dest, "wb") as f:
        shutil.copyfileobj(r, f, 1024*1024)

REPO = os.environ["GITHUB_REPOSITORY"]
CHAT = os.environ.get("TELEGRAM_CHAT_ID", "")

# ---------- 1) l0.smali patchado (20KB, transporte seguro) ----------
print("== l0.smali patchado")
sh(f"mkdir -p patch && curl -sL https://raw.githubusercontent.com/{REPO}/eni-inbox/patch/Gc_l0.smali -o patch/l0.smali")
l0 = open("patch/l0.smali").read()
assert "ENI: veredito" in l0, "l0.smali patchado nao veio (raw falhou)"
print("l0 patchado ok:", len(l0), "bytes")

# ---------- 2) deposito (artifact com PAT; fallback raw tez_parts) ----------
print("== deposito das partes")
os.makedirs("deposito", exist_ok=True)
if len(glob.glob("deposito/tezza_cm_*")) < 20:
    tok = os.environ.get("WORKFLOW_PAT") or os.environ.get("GH_TOKEN")
    arts = json.loads(urllib.request.urlopen(urllib.request.Request(
        f"https://api.github.com/repos/{REPO}/actions/artifacts?per_page=50",
        headers={"Authorization": f"Bearer {tok}", "Accept": "application/vnd.github+json"}), timeout=60).read())
    alvo = next(a for a in arts["artifacts"] if a["name"] == "deposito" and a["size_in_bytes"] > 300_000_000)
    import shlex
    subprocess.run(f"curl -sL -H \"Authorization: Bearer {tok}\" -o deposito.zip {shlex.quote(alvo['archive_download_url'])}", shell=True, check=True)
    zipfile.ZipFile("deposito.zip").extractall("deposito")
partes = sorted(glob.glob("deposito/tezza_cm_*"))
assert len(partes) >= 20, f"so {len(partes)} partes"
with open("tezza.xapk", "wb") as o:
    for p in partes:
        o.write(open(p, "rb").read())
print("xapk:", os.path.getsize("tezza.xapk"))

# ---------- 3) classes2.dex original -> smali -> troca l0 -> dex novo ----------
print("== assemble do dex do veredito (regenerado no runner)")
zx = zipfile.ZipFile("tezza.xapk")
zx.extract("org.tezza.apk", ".")
zb = zipfile.ZipFile("org.tezza.apk")
zb.extract("classes2.dex", ".")
zb.extract("AndroidManifest.xml", ".")
with zipfile.ZipFile("dummy.apk", "w") as z:
    mf = open("AndroidManifest.xml", "rb").read()
    z.writestr(zipfile.ZipInfo("AndroidManifest.xml"), mf)
    zi = zipfile.ZipInfo("classes2.dex"); zi.compress_type = zipfile.ZIP_STORED
    z.writestr(zi, open("classes2.dex", "rb").read())
sh("sudo apt-get update -qq && sudo apt-get install -y -qq apktool", check=False)
rc, out = sh("apktool --version", check=False)
print("apktool do apt:", out.strip()[:40])
APK = "apktool"
print("dummy entries:", [(i.filename, i.file_size) for i in zipfile.ZipFile("dummy.apk").infolist()])
rc, out = sh(f"{APK} d dummy.apk -o smali_orig -f --no-res", check=False)
print("apktool d rc:", rc, "|", out[-200:])
candidatos = glob.glob("smali_orig/**/Gc/l0.smali", recursive=True)
print("l0 desmontado em:", candidatos, "| dirs:", sorted(os.listdir("smali_orig"))[:6])
assert candidatos, "l0 nao desmontou"
shutil.copy("patch/l0.smali", candidatos[0])
print("l0 patchado no lugar")
# manequim com manifest minimo: o aapt do build falhava nas referencias do manifest real
open("smali_orig/AndroidManifest.xml", "w").write(
    '<?xml version="1.0" encoding="utf-8"?>\n'
    '<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.eni.tez" '
    'android:versionCode="1" android:versionName="1.0">\n'
    '<uses-sdk android:minSdkVersion="24" android:targetSdkVersion="34"/>\n'
    '</manifest>\n')
subprocess.run("rm -rf smali_orig/res smali_orig/assets smali_orig/original smali_orig/apktool.yml", shell=True)
print("manequim enxugado (manifest minimo, sem res)")
proc = subprocess.Popen(f"{APK} b smali_orig -o mod.apk > build_int.log 2>&1", shell=True, start_new_session=True)
sh("sudo apt-get install -y -qq smali", check=False)
rc, out = sh("smali assemble smali_orig/smali_classes2 -o classes2_new.dex", check=False)
print("smali assemble:", rc, "|", out[-150:].replace("\n", " "))
if rc != 0 or not (os.path.exists("classes2_new.dex") and open("classes2_new.dex", "rb").read(4) == b"dex\n"):
    print("smali puro nao foi - fallback apktool b")
    shutil.rmtree("smali_orig/build", ignore_errors=True)
    proc = subprocess.Popen(f"{APK} b smali_orig -o mod.apk > build_int.log 2>&1", shell=True, start_new_session=True)
    dex_src_c = glob.glob("smali_orig/build/apk/classes*.dex")
    dex_src = dex_src_c[0] if dex_src_c else "smali_orig/build/apk/classes.dex"
    ultimo, estavel, dex_resgatado = -1, 0, None
    for _ in range(200):
        time.sleep(3)
        if os.path.exists(dex_src):
            tam = os.path.getsize(dex_src)
            if tam == ultimo and tam > 1000000:
                estavel += 1
                if estavel >= 2:
                    dex_resgatado = open(dex_src, "rb").read()
                    break
            else:
                estavel = 0
            ultimo = tam
    proc.terminate(); time.sleep(2); proc.kill()
    try:
        print("--- build_int.log ---")
        print(open("build_int.log").read()[-600:])
    except Exception:
        pass
    assert dex_resgatado and dex_resgatado[:4] == b"dex\n", "dex nao resgatado"
    open("classes2_new.dex", "wb").write(dex_resgatado)
print("dex do veredito:", os.path.getsize("classes2_new.dex"), "bytes")
try:
    print("--- build_int.log ---")
    print(open("build_int.log").read()[-400:])
except Exception:
    pass

# ---------- 4) montagem do universal ----------
print("== montando o universal")
shutil.rmtree("base_work", ignore_errors=True)
zipfile.ZipFile("org.tezza.apk").extractall("base_work")
for d in ["classes.dex", "classes2.dex"]:
    if os.path.exists(f"base_work/{d}"):
        os.remove(f"base_work/{d}")
shutil.copy("classes2_new.dex", "base_work/classes2.dex")
zx.extract("config.arm64_v8a.apk", ".")
zipfile.ZipFile("config.arm64_v8a.apk").extractall("arm64_work")
nlibs = 0
for raiz, _, files in os.walk("arm64_work"):
    for f in files:
        if f.endswith(".so"):
            rel = os.path.relpath(os.path.join(raiz, f), "arm64_work")
            dst = os.path.join("base_work", rel)
            os.makedirs(os.path.dirname(dst), exist_ok=True)
            shutil.copy2(os.path.join(raiz, f), dst)
            nlibs += 1
print(f"libs arm64: {nlibs}")

import struct
mp = "base_work/AndroidManifest.xml"
data = bytearray(open(mp, "rb").read())
(hsize,) = struct.unpack_from("<H", data, 2)
off = hsize
for _ in range(40):
    if off + 8 > len(data): break
    t, hs, sz = struct.unpack_from("<HHI", data, off)
    if t == 0x0001:
        n, _, fl, ss, _ = struct.unpack_from("<IIIII", data, off + 8)
        base_o = off + 28
        for i in range(n):
            (so,) = struct.unpack_from("<I", data, base_o + 4*i)
            q = off + ss + so
            l2 = data[q+1]
            s = bytes(data[q+2:q+2+l2])
            if s in (b"requiredSplitTypes", b"isSplitRequired"):
                data[q+2:q+2+l2] = b"\x00" * l2
                print("manifest remendado:", s.decode())
        break
    off += sz
open(mp, "wb").write(bytes(data))

def regra(rel):
    return rel == "resources.arsc" or rel.startswith("lib/") or rel.endswith(".so")

zin = zipfile.ZipFile("org.tezza.apk")
zout = zipfile.ZipFile("base_mod.apk", "w", zipfile.ZIP_DEFLATED)
for item in zin.infolist():
    if item.filename.endswith(".dex") or item.filename.startswith("lib/"):
        continue
    zi = zipfile.ZipInfo(item.filename, date_time=item.date_time)
    zi.compress_type = zipfile.ZIP_STORED if regra(item.filename) else zipfile.ZIP_DEFLATED
    src = os.path.join("base_work", item.filename)
    zout.writestr(zi, open(src, "rb").read() if os.path.exists(src) else zin.read(item.filename))
if os.path.isdir("base_work/lib"):
    for raiz, _, files in os.walk("base_work/lib"):
        for f in files:
            rel = os.path.relpath(os.path.join(raiz, f), "base_work")
            zi = zipfile.ZipInfo(rel); zi.compress_type = zipfile.ZIP_STORED
            zout.writestr(zi, open(os.path.join(raiz, f), "rb").read())
zi = zipfile.ZipInfo("classes.dex"); zi.compress_type = zipfile.ZIP_STORED
zout.writestr(zi, zin.read("classes.dex"))
zi = zipfile.ZipInfo("classes2.dex"); zi.compress_type = zipfile.ZIP_STORED
zout.writestr(zi, open("classes2_new.dex", "rb").read())
zout.close()
print("classes.dex original + classes2.dex patchado no pacote")
print("base_mod.apk:", os.path.getsize("base_mod.apk"))

# ---------- 5) assinatura + entrega ----------
sh("wget -q https://dl.google.com/android/repository/build-tools_r34-linux.zip && unzip -q build-tools_r34-linux.zip -d bt")
sh("base64 -d eni.keystore.b64 > eni.keystore")
env = 'LD_LIBRARY_PATH=$PWD/bt/android-14/lib64'
sh(f"{env} bt/android-14/zipalign -f -p 4 base_mod.apk alinhado.apk")
sh(f"java -jar bt/android-14/lib/apksigner.jar sign --ks eni.keystore --ks-pass pass:eni123456 --key-pass pass:eni123456 --ks-key-alias eni --out tezza-mod.apk alinhado.apk")
rc, _ = sh("java -jar bt/android-14/lib/apksigner.jar verify --print-certs tezza-mod.apk")
assert rc == 0, "assinatura falhou"
print("assinado:", os.path.getsize("tezza-mod.apk"), "bytes")

TOK = os.environ["TELEGRAM_TOKEN"]
def envia(caminho, legenda):
    for _ in range(3):
        r = subprocess.run(["curl", "-s", "-F", f"chat_id={CHAT}", "-F", f"document=@{caminho}",
                            "-F", f"caption={legenda}", f"https://api.telegram.org/bot{TOK}/sendDocument"],
                           capture_output=True, text=True, timeout=900)
        if r.returncode == 0 and '"ok":true' in r.stdout:
            return True
        time.sleep(8)
    return False

tam = os.path.getsize("tezza-mod.apk")
if tam <= 20_000_000:
    envia("tezza-mod.apk", f"Tezza mod - {tam} bytes (assinado ENI)")
else:
    subprocess.run("split -b 18000000 -d -a 2 tezza-mod.apk tezza_out_", shell=True, check=True)
    for f in sorted(glob.glob("tezza_out_*")):
        assert envia(f, os.path.basename(f)), f"fatia {f} falhou"
    urllib.request.urlopen(urllib.request.Request(f"https://api.telegram.org/bot{TOK}/sendMessage",
        data=json.dumps({"chat_id": CHAT, "text": f"Tezza mod em fatias - junta: cat tezza_out_* > tezza-mod.apk ({tam} bytes)"}).encode(),
        headers={"Content-Type": "application/json"}), timeout=60)
print("ENTREGUE no telegram")
print("FIM DO TRAMPO")
