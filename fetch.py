#!/usr/bin/env python3
import sys, os, requests
from gpapi.googleplay import GooglePlayAPI
from gpapi import googleplay_pb2, config

PKG = open("target.txt").read().strip() if os.path.exists("target.txt") else sys.argv[1]

def anon_gsfid():
    req = googleplay_pb2.AndroidCheckinRequest()
    req.id = 0
    req.checkin.CopyFrom(config.getAndroidCheckinRequest("angler"))
    req.locale = "en_US"
    req.timeZone = "UTC"
    req.version = 3
    req.deviceConfiguration.CopyFrom(config.getDeviceConfiguration("angler"))
    req.fragment = 0
    r = requests.post("https://android.clients.google.com/checkin",
                      data=req.SerializeToString(),
                      headers={"Content-Type": "application/x-protobuf"}, timeout=40)
    resp = googleplay_pb2.AndroidCheckinResponse()
    resp.ParseFromString(r.content)
    return resp.androidId

def get_aas():
    for url in ["https://auroraoss.com/api/auth",
                "https://auroraoss.in/api/auth",
                "https://dispenser.auroraoss.com/api/auth"]:
        try:
            t = requests.get(url, timeout=25).text.strip()
            if ":" in t and "@" in t:
                print("dispenser:", url)
                return t.split(":", 1)
        except Exception:
            continue
    return None, None

email, aas = get_aas()
if not aas:
    print("dispenser falhou — caindo pra senha de app")
    email = "worldeffects752@gmail.com"
    aas = None

api = GooglePlayAPI()
if aas:
    api.gsfId = anon_gsfid()
    api.setAuthSubToken(aas)
    api.search("firefox", 1, None)
    print("login AAS OK —", email, "gsfid", api.gsfId)
else:
    api.login(email, "zwkloouasmecmhlt")
    print("login senha OK — gsfid:", api.gsfId)

det = api.details(PKG)
vc = det["details"]["appDetails"]["versionCode"]
res = api.download(PKG, vc)
data = res[0] if isinstance(res, tuple) else res
open(f"{PKG}.apk", "wb").write(data)
print("BAIXADO:", PKG, "vc", vc, len(data), "bytes")
