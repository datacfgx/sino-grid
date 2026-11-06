#!/usr/bin/env python3
import sys
from gpapi.googleplay import GooglePlayAPI

EMAIL = "worldeffects752@gmail.com"
PASSWORD = "293847Ala@#"
PKG = sys.argv[1]

api = GooglePlayAPI(locale="en_US", timezone="UTC", device_codename="angler")
api.login(EMAIL, PASSWORD, None, None)
print("login OK — gsfid:", getattr(api, "gsfId", "?"))
res = api.download(PKG)
data = res[0] if isinstance(res, tuple) else res
open(f"{PKG}.apk", "wb").write(data)
print("BAIXADO:", PKG, len(data), "bytes")
