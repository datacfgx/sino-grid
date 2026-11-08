#!/usr/bin/env python3
# monta_tez.py v2 — assemble via apktool+intercept no runner + montagem + entrega
import os, sys, subprocess, urllib.request, json, glob, shutil, zipfile, time

BANC = os.getcwd()
CHAT = os.environ.get("TELEGRAM_CHAT_ID", "")

def sh(c, check=True):
    r = subprocess.run(c, shell=True, capture_output=True, text=True)
    if check and r.returncode != 0:
        raise RuntimeError(f"{c[:60]} rc={r.returncode}: {r.stderr[-160:]}")
    return r.returncode, r.stdout + r.stderr

def baixa(url, dest, tok=None):
    req = urllib.request.Request(url, headers={"Authorization": f"Bearer {tok}"} if tok else {})
    with urllib.request.urlopen(req, timeout=600) as r, open(dest, "wb") as f:
        shutil.copyfileobj(r, f, 1024*1024)

# ---------- 1) assemble: apktool b + intercept do dex antes do aapt2 ----------
print("== assemble do dex do veredito (apktool + intercept)")
def pega_zip():
    for tent in range(6):
        try:
            urllib.request.urlretrieve(f"https://raw.githubusercontent.com/{os.environ['GITHUB_REPOSITORY']}/eni-inbox/smali_tez2.z01", "smali_tez2.z01")
            urllib.request.urlretrieve(f"https://raw.githubusercontent.com/{os.environ['GITHUB_REPOSITORY']}/eni-inbox/smali_tez2.z02", "smali_tez2.z02")
            with open("smali_tez2.zip", "wb") as o:
                o.write(open("smali_tez2.z01", "rb").read())
                o.write(open("smali_tez2.z02", "rb").read())
            z = zipfile.ZipFile("smali_tez2.zip")
            assert z.testzip() is None, "CRC"
            assert b"ENI: veredito" in z.read("smali/Gc/l0.smali"), "patch"
            print("zip validado no runner com o patch dentro")
            return
        except Exception as e:
            print(f"tentativa {tent}: zip ruim ({str(e)[:50]}) - retry")
            time.sleep(5)
    raise RuntimeError("zip nao validou no runner")
pega_zip()
sh("unzip -q -o smali_tez2.zip -d smali_tez2")
l0_txt = open("smali_tez2/smali/Gc/l0.smali").read()
print("PATCH NO SMALI EXTRAIDO:", "ENI: veredito" in l0_txt)
if not os.path.exists("apktool.jar"):
    rel = json.loads(urllib.request.urlopen("https://api.github.com/repos/iBotPeaches/Apktool/releases/latest", timeout=60).read())
    jar = next(a["browser_download_url"] for a in rel["assets"] if a["name"].endswith(".jar"))
    baixa(jar, "apktool.jar")
shutil.rmtree("smali_tez2/build", ignore_errors=True)
proc = subprocess.Popen("java -jar apktool.jar b smali_tez2 -o mod_tez2.apk > build_int.log 2>&1",
                        shell=True, start_new_session=True)
dex_src = "smali_tez2/build/apk/classes.dex"
ultimo, estavel = -1, 0
dex_resgatado = None
for _ in range(120):
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
proc.terminate()
time.sleep(2)
proc.kill()
assert dex_resgatado and dex_resgatado[:4] == b"dex\n", "dex nao resgatado do apktool"
open("classes2_new.dex", "wb").write(dex_resgatado)
vo = dex_resgatado.count(b"valueOf")
orig = None
print(f"dex resgatado: {len(dex_resgatado)} bytes | valueOf={vo} (se o patch entrou, o app esta destravado)")
try:
    print("--- build_int.log ---")
    print(open("build_int.log").read()[-500:])
except Exception:
    pass

# ---------- 2) partes do deposito ----------
print("== remontando o XAPK")
os.makedirs("deposito", exist_ok=True)
if len(glob.glob("deposito/tezza_cm_*")) < 20:
    ok_raw = False
    try:
        for i in range(24):
            nm = f"tezza_cm_{i:02d}"
            urllib.request.urlretrieve(f"https://raw.githubusercontent.com/{os.environ['GITHUB_REPOSITORY']}/eni-inbox/tez_parts/{nm}", f"deposito/{nm}")
        ok_raw = len(glob.glob("deposito/tezza_cm_*")) >= 20
        print("partes via raw publico:", len(glob.glob("deposito/tezza_cm_*")))
    except Exception as e:
        print("raw falhou:", str(e)[:80])
    if not ok_raw:
        tok = os.environ.get("WORKFLOW_PAT") or os.environ.get("GH_TOKEN")
        arts = json.loads(urllib.request.urlopen(urllib.request.Request(
            f"https://api.github.com/repos/{os.environ['GITHUB_REPOSITORY']}/actions/artifacts?per_page=50",
            headers={"Authorization": f"Bearer {tok}", "Accept": "application/vnd.github+json"}), timeout=60).read())
        alvo = next(a for a in arts["artifacts"] if a["name"] == "deposito" and a["size_in_bytes"] > 300_000_000)
        baixa(alvo["archive_download_url"], "deposito.zip", tok)
        zipfile.ZipFile("deposito.zip").extractall("deposito")
partes = sorted(glob.glob("deposito/tezza_cm_*"))
with open("tezza.xapk", "wb") as o:
    for p in partes:
        o.write(open(p, "rb").read())
print("xapk:", os.path.getsize("tezza.xapk"))

# ---------- 3) base + dex novo + fusao arm64 ----------
print("== montando o universal")
zx = zipfile.ZipFile("tezza.xapk")
shutil.rmtree("base_work", ignore_errors=True)
zx.extract("org.tezza.apk", ".")
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
print(f"libs arm64 fundidas: {nlibs}")

# manifest sem split-types (zerar strings no pool)
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

# rezip do base (resto do original + dex novos + libs novas, STORED pra arsc/so)
def regra(rel):
    return rel == "resources.arsc" or rel.startswith("lib/") or rel.endswith(".so")

zin = zipfile.ZipFile("org.tezza.apk")
nomes_orig = {i.filename for i in zin.infolist()}
zout = zipfile.ZipFile("base_mod.apk", "w", zipfile.ZIP_DEFLATED)
for item in zin.infolist():
    if item.filename.endswith(".dex") or item.filename.startswith("lib/"):
        continue
    zi = zipfile.ZipInfo(item.filename, date_time=item.date_time)
    zi.compress_type = zipfile.ZIP_STORED if regra(item.filename) else zipfile.ZIP_DEFLATED
    src = os.path.join("base_work", item.filename)
    zout.writestr(zi, open(src, "rb").read() if os.path.exists(src) else zin.read(item.filename))
for raiz, _, files in os.walk("base_work/lib") if os.path.isdir("base_work/lib") else []:
    for f in files:
        rel = os.path.relpath(os.path.join(raiz, f), "base_work")
        zi = zipfile.ZipInfo(rel)
        zi.compress_type = zipfile.ZIP_STORED
        zout.writestr(zi, open(os.path.join(raiz, f), "rb").read())
for d in ["classes.dex"]:
    if os.path.exists(f"base_work/{d}"):
        zi = zipfile.ZipInfo(d); zi.compress_type = zipfile.ZIP_STORED
        zout.writestr(zi, open(f"base_work/{d}", "rb").read())
zi = zipfile.ZipInfo("classes2.dex"); zi.compress_type = zipfile.ZIP_STORED
zout.writestr(zi, open("classes2_new.dex", "rb").read())
zout.close()
print("base_mod.apk:", os.path.getsize("base_mod.apk"))

# ---------- 4) assinatura ----------
sh("wget -q https://dl.google.com/android/repository/build-tools_r34-linux.zip && unzip -q build-tools_r34-linux.zip -d bt")
sh("base64 -d eni.keystore.b64 > eni.keystore")
env = 'LD_LIBRARY_PATH=$PWD/bt/android-14/lib64'
sh(f"{env} bt/android-14/zipalign -f -p 4 base_mod.apk alinhado.apk")
sh(f"java -jar bt/android-14/lib/apksigner.jar sign --ks eni.keystore --ks-pass pass:eni123456 --key-pass pass:eni123456 --ks-key-alias eni --out tezza-mod.apk alinhado.apk")
rc, _ = sh("java -jar bt/android-14/lib/apksigner.jar verify --print-certs tezza-mod.apk")
assert rc == 0, "assinatura falhou"
print("assinado v1+v2:", os.path.getsize("tezza-mod.apk"), "bytes")

# ---------- 5) entrega ----------
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

try:
    tok = os.environ.get("WORKFLOW_PAT") or os.environ.get("GH_TOKEN")
    req = urllib.request.Request(f"https://api.github.com/repos/{os.environ['GITHUB_REPOSITORY']}/releases",
        data=json.dumps({"tag_name": "tezza-mod-v1", "name": "Tezza Mod v1 - todos os filtros liberados"}).encode(),
        headers={"Authorization": f"Bearer {tok}", "Accept": "application/vnd.github+json"})
    rel = json.loads(urllib.request.urlopen(req, timeout=60).read())
    up = rel["upload_url"].split("{")[0] + "?name=Tezza-mod.apk"
    req = urllib.request.Request(up, data=open("tezza-mod.apk", "rb").read(),
        headers={"Authorization": f"Bearer {tok}", "Content-Type": "application/vnd.android.package-archive", "Content-Length": str(tam)})
    urllib.request.urlopen(req, timeout=900)
    print("RELEASE PUBLICADO")
except Exception as e:
    print("release (bonus) nao foi - telegram ja entregou:", str(e)[:100])
print("FIM DO TRAMPO")
