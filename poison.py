#!/usr/bin/env python3
import sys, os, zipfile, shutil
MODO = sys.argv[1] if len(sys.argv) > 1 else "sombra"
SUBS_TODOS = [
    (b"volceapplog.com",     b"127.0.0.1.xxxxx"),
    (b"volctrack.com",       b"127.0.0.1.xxx"),
    (b"crash.163.com",       b"127.0.0.1.cra"),
    (b"app-measurement.com", b"127.0.0.1.xxxxxxxxx"),
    (b"google-analytics.com", b"127.0.0.1.xxxxxxxxxx"),
    (b"volces.com",          b"127.0.0.1."),
]
SUBS = {"sombra": SUBS_TODOS, "controle": []}[MODO]
for old, new in SUBS: assert len(old) == len(new), old
STORED_EXTS = (".gif", ".jpg", ".png", ".webp", ".prof", ".profm", ".tflite")
shutil.rmtree("work", ignore_errors=True)
os.makedirs("work", exist_ok=True)
with zipfile.ZipFile("base.apk") as z: z.extractall("work")
total = 0
if SUBS:
    for root, _, fs in os.walk("work"):
        for f in fs:
            if not (f.endswith(".dex") or f == "resources.arsc"): continue
            path = os.path.join(root, f)
            data = open(path, "rb").read()
            n = 0
            for old, new in SUBS:
                c = data.count(old)
                if c: data = data.replace(old, new); n += c
            if n: open(path, "wb").write(data)
            total += n
    if total < 5:
        print(f"FALHA: so {total}"); sys.exit(1)
print(f"MODO={MODO}: {total} envenenamentos na base")
saida = f"kimi-int-{MODO}.apk"
with zipfile.ZipFile(saida, "w") as z:
    for root, _, fs in os.walk("work"):
        for f in fs:
            full = os.path.join(root, f)
            rel = os.path.relpath(full, "work")
            stored = (rel == "resources.arsc" or rel.startswith("lib/") or rel.endswith(STORED_EXTS))
            z.write(full, rel, compress_type=zipfile.ZIP_STORED if stored else zipfile.ZIP_DEFLATED)
with zipfile.ZipFile(saida) as z:
    ruins = [i.filename for i in z.infolist()
             if (i.filename == "resources.arsc" or i.filename.startswith("lib/") or i.filename.endswith(STORED_EXTS)) and i.compress_type != 0]
    if ruins: print("NAO-STORED:", ruins[:3]); sys.exit(1)
print(f"{saida} OK ({os.path.getsize(saida)} bytes)")
