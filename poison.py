#!/usr/bin/env python3
# Kimi Sombra — intoxicacao binaria de trackers (substituicao same-length nos dex/arsc)
import sys, os, zipfile, shutil

SUBS = [
    (b"kimi.volctrack.com",      b"127.0.0.1.volc.com"),
    (b"abtest.volceapplog.com",  b"127.0.0.1.xxxxxxxxxxxx"),
    (b"alink.volceapplog.com",   b"127.0.0.1.xxxxxxxxxxx"),
    (b"klink.volceapplog.com",   b"127.0.0.1.xxxxxxxxxxx"),
    (b"tobapplog.volceapplog.com", b"127.0.0.1.xxxxxxxxxxxxxxx"),
    (b"toblog.volceapplog.com",  b"127.0.0.1.xxxxxxxxxxxx"),
    (b"crash.163.com",           b"127.0.0.1.cra"),
]
for old, new in SUBS:
    assert len(old) == len(new), f"tamanho diferente: {old}"

os.makedirs("work", exist_ok=True)
with zipfile.ZipFile("original.apk") as z:
    z.extractall("work")

alvos = [f"work/classes{i}.dex" for i in ["", "2", "3"]]
if os.path.exists("work/resources.arsc"):
    alvos.append("work/resources.arsc")

total = 0
for path in alvos:
    data = open(path, "rb").read()
    n = 0
    for old, new in SUBS:
        c = data.count(old)
        if c:
            data = data.replace(old, new)
            n += c
    open(path, "wb").write(data)
    print(f"{path}: {n} substituicoes")
    total += n

if total < 7:
    print(f"FALHA: so {total} substituicoes (esperado >=7)")
    sys.exit(1)
print(f"TOTAL: {total} envenenamentos")

# rezip no original.apk (substitui dex modificados)
with zipfile.ZipFile("original.apk", "a", zipfile.ZIP_DEFLATED) as z:
    for path in alvos:
        z.write(path, path.replace("work/", ""))
print("APK rezipado")
