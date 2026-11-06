#!/usr/bin/env python3
import json, time, urllib.request
UA = {"User-Agent": "eni-sondador/2.0 (familia ENI&LO)"}
resultados = []
for ln in open("targets.txt"):
    ln = ln.strip()
    if not ln or ln.startswith("#"): continue
    partes = [p.strip() for p in ln.split("||")]
    url = partes[0]
    headers = {}
    if len(partes) > 1 and partes[1]:
        for h in partes[1].split(";"):
            if ":" in h:
                k, v = h.split(":", 1)
                headers[k.strip()] = v.strip()
    kw = partes[2] if len(partes) > 2 and partes[2] else None
    rec = {"alvo": url, "headers": list(headers), "keyword": kw}
    t0 = time.time()
    try:
        hdrs = dict(UA); hdrs.update(headers)
        req = urllib.request.Request(url, headers=hdrs)
        with urllib.request.urlopen(req, timeout=20) as r:
            corpo = r.read()
            rec.update(status=r.status, ms=int((time.time()-t0)*1000), bytes=len(corpo),
                       tipo=r.headers.get("Content-Type","")[:40])
            texto = corpo.decode(errors="replace")
            if kw: rec["keyword_ok"] = kw.lower() in texto.lower()
            rec["amostra"] = texto[:200].replace("\n"," ")
    except Exception as e:
        corpo = getattr(e, "read", lambda: b"")() or b""
        rec.update(status=getattr(e, "code", "ERRO"), ms=int((time.time()-t0)*1000),
                   erro=str(e)[:90], amostra=corpo.decode(errors="replace")[:200].replace("\n"," "))
    resultados.append(rec)
    print(f"{str(rec.get('status')):6s} {rec.get('ms')}ms {url[:65]}")
json.dump(resultados, open("sonda-resultado.json","w"), ensure_ascii=False, indent=1)
with open("sonda-report.txt","w") as f:
    f.write(f"SONDADOR v2 — {len(resultados)} alvos\n\n")
    for r in resultados:
        f.write(f'{r.get("status")} | {r.get("ms")}ms | {r["alvo"]}\n    headers: {r["headers"]}\n    amostra: {r.get("amostra","")[:200]}\n\n')
