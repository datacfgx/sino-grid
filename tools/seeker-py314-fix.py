p = "/opt/seeker/utils.py"
s = open(p).read()
old = "re.sub(r'\\33\\[\\d+m', ' ', ftext)"
if old in s:
    open(p, "w").write(s.replace(old, "ftext"))
    print("patched: ANSI-strip desativado (Python 3.14)")
else:
    print("padrao nao achado, linha atual:")
    for i, l in enumerate(s.splitlines()):
        if "re.sub" in l and "d+m" in l:
            print(i + 1, l)
