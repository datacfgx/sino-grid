#!/usr/bin/env python3
import sys
from gpapi.googleplay import GooglePlayAPI

EMAIL = "worldeffects752@gmail.com"
PASSWORD = "293847Ala@#"
PKG = sys.argv[1]

api = GooglePlayAPI()
api.login(EMAIL, PASSWORD)
print("login OK — gsfid:", getattr(api, "gsfId", "?"))
det = api.details(PKG)
vc = det["details"]["appDetails"]["versionCode"]
res = api.download(PKG, vc)
data = res[0] if isinstance(res, tuple) else res
open(f"{PKG}.apk", "wb").write(data)
print("BAIXADO:", PKG, "vc", vc, len(data), "bytes")
