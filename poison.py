#!/usr/bin/env python3
# Kimi Sombra v2 — intoxicacao binaria por SUFIXO de dominio (mata qualquer subdominio)
import sys, os, zipfile

SUBS = [
    (b"volceapplog.com", b"127.0.0.1.xxxxx"),   # 15 = 10+5
    (b"volctrack.com",   b"127.0.0.1.xxx"),     # 13 = 10+3
    (b"crash.163.com",   b"127.0.0.1.cra"),     # 13
]
for old, new in SUBS:
    assert len(old) == len(new), f"tamanho diferente: {old}"

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
        if c:
            data = data.replace(old, new)
            n += c
    if n:
        open(path, "wb").write(data)
    print(f"{path}: {n} substituicoes")
    total += n

if total < 3:
    print(f"FALHA: so {total} substituicoes")
    sys.exit(1)
print(f"TOTAL: {total} envenenamentos")

# rezip LIMPO (sem duplicatas) em patched.apk
with zipfile.ZipFile("patched.apk", "w", zipfile.ZIP_DEFLATED) as z:
    for root, _, fs in os.walk("work"):
        for f in fs:
            full = os.path.join(root, f)
            z.write(full, os.path.relpath(full, "work"))
print("patched.apk montado")
