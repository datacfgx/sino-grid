#!/usr/bin/env python3
import sys, os, zipfile, shutil
SUBS = [
    (b"volceapplog.com", b"127.0.0.1.xxxxx"),
    (b"volctrack.com",   b"127.0.0.1.xxx"),
    (b"crash.163.com",   b"127.0.0.1.cra"),
]
for old, new in SUBS:
    assert len(old) == len(new)
os.makedirs("work", exist_ok=True)
with zipfile.ZipFile("original.apk") as z:
    z.extractall("work")
alvos = []
for root, _, fs in os.walk("work"):
    for f in fs:
        if f.endswith(".dex") or f == "resources.arsc":
            alvos.append(os.path.join(root, f))
total = 0
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
print(f"TOTAL: {total} envenenamentos")
# rezip: lib/ STORED (extractNativeLibs), resto DEFLATED
with zipfile.ZipFile("patched.apk", "w") as z:
    for root, _, fs in os.walk("work"):
        for f in fs:
            full = os.path.join(root, f)
            rel = os.path.relpath(full, "work")
            ct = zipfile.ZIP_STORED if rel.startswith("lib/") else zipfile.ZIP_DEFLATED
            z.write(full, rel, compress_type=ct)
# valida: nenhuma .so comprimida
with zipfile.ZipFile("patched.apk") as z:
    ruins = [i.filename for i in z.infolist() if i.filename.endswith(".so") and i.compress_type != 0]
    if ruins:
        print("SO COMPRIMIDA:", ruins[:3]); sys.exit(1)
print("lib/ STORED validado ✔")
shutil.copyfile("patched.apk", "original.apk")
print("original.apk envenenado e alinhavel")
