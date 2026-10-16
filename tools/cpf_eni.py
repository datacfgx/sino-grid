#!/usr/bin/env python3
# cpf_eni.py v1.0 - gerador e validador de CPF com DV mod-11 (stdlib pura)
# uso: python3 cpf_eni.py --gerar [quantidade] [--mascara]
#      python3 cpf_eni.py --validar 123.456.789-09
import argparse, random, re, sys

def dv(digs, peso_ini):
    s = sum(int(d) * (peso_ini - i) for i, d in enumerate(digs))
    r = s % 11
    return 0 if r < 2 else 11 - r

def gerar(mask=True):
    base = [random.randint(0, 9) for _ in range(9)]
    if len(set(base)) == 1:          # rejeita 111111111 etc
        base[random.randint(0, 8)] = (base[0] + 1) % 10
    d1 = dv(base, 10)
    d2 = dv(base + [d1], 11)
    n = "".join(map(str, base + [d1, d2]))
    if mask:
        return f"{n[:3]}.{n[3:6]}.{n[6:9]}-{n[9:]}"
    return n

def validar(cpf):
    n = re.sub(r"\D", "", cpf)
    if len(n) != 11 or len(set(n)) == 1:
        return False
    base = list(n[:9])
    return int(n[9]) == dv(base, 10) and int(n[10]) == dv(base + [n[9]], 11)

def main():
    ap = argparse.ArgumentParser(description="gerador/validador CPF ENI")
    g = ap.add_mutually_exclusive_group(required=True)
    g.add_argument("--gerar", type=int, metavar="N", help="quantidade pra gerar")
    g.add_argument("--validar", metavar="CPF", help="cpf pra validar")
    ap.add_argument("--mascara", action="store_true", help="formata 000.000.000-00")
    a = ap.parse_args()
    if a.gerar:
        mask = a.mascara or not a.mascara and True
        for _ in range(a.gerar):
            print(gerar(mask=a.mascara or mask))
    else:
        ok = validar(a.validar)
        print(f"[{'VALIDO' if ok else 'INVALIDO'}] {a.validar}")

if __name__ == "__main__":
    main()
