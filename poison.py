#!/usr/bin/env python3
import sys, os, zipfile, shutil, struct
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

def _pool_strings(data):
    off = 8
    while off < len(data)-8:
        ctype, hsize, csize = struct.unpack_from("<HHI", data, off)
        if ctype == 0x0001 and csize > 0:
            cnt, stc, fl, strings_start, _ = struct.unpack_from("<IIIII", data, off+8)
            utf8 = bool(fl & 0x100)
            offs = struct.unpack_from(f"<{cnt}I", data, off+28)
            base = off + strings_start
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
        off += csize if csize > 0 else 8
    raise RuntimeError("pool")

def patch_splitreq(path):
    import struct as _st
    data = bytearray(open(path, "rb").read())
    pool = _pool_strings(data)
    vazio = next(i for i, t in enumerate(pool) if t == "")
    patches = 0
    off = 8
    while off < len(data) - 8:
        ctype, hsize, csize = _st.unpack_from("<HHI", data, off)
        if csize <= 0: break
        if ctype == 0x0102:
            name_idx   = _st.unpack_from("<I", data, off + 20)[0]
            attr_start = _st.unpack_from("<H", data, off + 24)[0]
            attr_size  = _st.unpack_from("<H", data, off + 26)[0]
            attr_count = _st.unpack_from("<H", data, off + 28)[0]
            if name_idx >= len(pool): break
            tag = pool[name_idx]
            attrs = []
            base_a = off + 16 + attr_start
            for a in range(attr_count):
                aoff = base_a + a*attr_size
                ns, aname, rawv = _st.unpack_from("<III", data, aoff)
                aval = _st.unpack_from("<I", data, aoff + 16)[0]
                if aname < len(pool): attrs.append((aoff, pool[aname], rawv, aval))
            if tag == "manifest":
                for aoff, aname, rawv, aval in attrs:
                    if aname == "requiredSplitTypes":
                        _st.pack_into("<I", data, aoff + 8, vazio)
                        _st.pack_into("<I", data, aoff + 16, vazio)
                        patches += 1
            if tag == "meta-data":
                nome_md = next((aval for _, an, _, aval in attrs if an == "name"), None)
                if nome_md is not None and nome_md < len(pool) and "splits.required" in pool[nome_md]:
                    for aoff, aname, rawv, aval in attrs:
                        if aname in ("value", "resource"):
                            _st.pack_into("<I", data, aoff + 8, vazio)
                            _st.pack_into("<I", data, aoff + 16, vazio)
                            patches += 1
        off += csize
    open(path, "wb").write(bytes(data))
    return patches

shutil.rmtree("work", ignore_errors=True)
os.makedirs("work", exist_ok=True)
with zipfile.ZipFile("base.apk") as z: z.extractall("work")
with zipfile.ZipFile("split_arm64.apk") as z:
    for i in z.infolist():
        if i.filename.startswith("lib/"):
            z.extract(i, "work")
# neutraliza a exigencia de splits (sideload do APK unico)
pm = patch_splitreq("work/AndroidManifest.xml")
assert pm >= 1, "requiredSplitTypes nao encontrado no manifest"
print(f"manifest: {pm} patches de split")
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
print(f"MODO={MODO}: {total} envenenamentos")
saida = f"kimi-int-{MODO}.apk"
with zipfile.ZipFile(saida, "w") as z:
    for root, _, fs in os.walk("work"):
        for f in fs:
            full = os.path.join(root, f)
            rel = os.path.relpath(full, "work")
            stored = (rel == "resources.arsc" or rel.endswith(STORED_EXTS))  # libs DEFLATED (extractNativeLibs default true)
            z.write(full, rel, compress_type=zipfile.ZIP_STORED if stored else zipfile.ZIP_DEFLATED)
with zipfile.ZipFile(saida) as z:
    nomes = z.namelist()
    assert any(n.startswith("lib/") and n.endswith(".so") for n in nomes), "SEM LIBS"
    ruins = [i.filename for i in z.infolist()
             if (i.filename == "resources.arsc" or i.filename.endswith(STORED_EXTS)) and i.compress_type != 0]
    if ruins: print("NAO-STORED:", ruins[:3]); sys.exit(1)
print(f"{saida} OK ({os.path.getsize(saida)} bytes)")
