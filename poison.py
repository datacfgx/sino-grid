#!/usr/bin/env python3
import sys, os, zipfile
MODO = sys.argv[1] if len(sys.argv) > 1 else "sombra"
TODOS = {
    "applog":  (b"volceapplog.com", b"127.0.0.1.xxxxx"),
    "track":   (b"volctrack.com",   b"127.0.0.1.xxx"),
    "crash":   (b"crash.163.com",   b"127.0.0.1.cra"),
}
SUBS = {"sombra": list(TODOS.values()), "controle": []}
for k, v in TODOS.items():
    SUBS[f"sem-{k}"] = [x for kk, x in TODOS.items() if kk != k]
if MODO not in SUBS:
    print("modo invalido:", MODO); sys.exit(1)
subs = SUBS[MODO]
for old, new in subs: assert len(old) == len(new)
STORED_EXTS = (".gif", ".jpg", ".png", ".webp", ".prof", ".profm", ".tflite")
import shutil
shutil.rmtree("work", ignore_errors=True)
os.makedirs("work", exist_ok=True)
with zipfile.ZipFile("original.apk") as z: z.extractall("work")
total = 0
if subs:
    for root, _, fs in os.walk("work"):
        for f in fs:
            if not (f.endswith(".dex") or f == "resources.arsc"): continue
            path = os.path.join(root, f)
            data = open(path, "rb").read()
            n = 0
            for old, new in subs:
                c = data.count(old)
                if c: data = data.replace(old, new); n += c
            if n: open(path, "wb").write(data)
            total += n
    if total < 1:
        print(f"FALHA: {total} substituicoes no modo {MODO}"); sys.exit(1)
saida = f"kimi-{MODO}.apk"
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
    if ruins: print("NAO-STORED:", ruins[:3]); sys.exit(1)
print(f"MODO={MODO}: {total} envenenamentos, {saida} ({os.path.getsize(saida)} bytes)")
