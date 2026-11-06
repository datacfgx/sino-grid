#!/usr/bin/env python3
import sys, os, zipfile, shutil
MODO = sys.argv[1] if len(sys.argv) > 1 else "sombra"
SUBS = [
    (b"volceapplog.com", b"127.0.0.1.xxxxx"),
    (b"volctrack.com",   b"127.0.0.1.xxx"),
    (b"crash.163.com",   b"127.0.0.1.cra"),
]
for old, new in SUBS: assert len(old) == len(new)
STORED_EXTS = (".gif", ".jpg", ".png", ".webp", ".prof", ".profm", ".tflite")
shutil.rmtree("work", ignore_errors=True)
os.makedirs("work", exist_ok=True)
with zipfile.ZipFile("original.apk") as z: z.extractall("work")
total = 0
if MODO == "sombra":
    alvos = []
    for root, _, fs in os.walk("work"):
        for f in fs:
            if f.endswith(".dex") or f == "resources.arsc":
                alvos.append(os.path.join(root, f))
    for path in alvos:
        data = open(path, "rb").read()
        n = 0
        for old, new in SUBS:
            c = data.count(old)
            if c: data = data.replace(old, new); n += c
        if n: open(path, "wb").write(data)
        total += n
    if total < 3:
        print(f"FALHA: {total}"); sys.exit(1)
print(f"MODO={MODO} envenenamentos: {total}")
saida = "patched.apk" if MODO == "sombra" else "controle.apk"
with zipfile.ZipFile(saida, "w") as z:
    for root, _, fs in os.walk("work"):
        for f in fs:
            full = os.path.join(root, f)
            rel = os.path.relpath(full, "work")
            stored = (rel == "resources.arsc" or rel.endswith(STORED_EXTS))
            z.write(full, rel, compress_type=zipfile.ZIP_STORED if stored else zipfile.ZIP_DEFLATED)
with zipfile.ZipFile(saida) as z:
    ruins = [i.filename for i in z.infolist()
             if (i.filename == "resources.arsc" or i.filename.endswith(STORED_EXTS)) and i.compress_type != 0]
    if ruins:
        print("NAO-STORED:", ruins[:5]); sys.exit(1)
print(f"{saida} OK ({os.path.getsize(saida)} bytes)")
