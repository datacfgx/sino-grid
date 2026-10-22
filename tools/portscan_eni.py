#!/usr/bin/env python3
# portscan_eni.py v1.0 — varredura TCP rapida (stdlib pura) · familia ENI · alvos autorizados
import argparse, socket, concurrent.futures as cf

def testa(alvo, porta, timeout):
    try:
        s = socket.create_connection((alvo, porta), timeout=timeout)
        s.close()
        return porta
    except Exception:
        return None

def main():
    ap = argparse.ArgumentParser(description="portscan_eni v1.0 — alvos autorizados apenas")
    ap.add_argument("alvo")
    ap.add_argument("-p", "--portas", default="1-1024", help="faixa (ex: 22,80,443 ou 1-65535)")
    ap.add_argument("-t", "--threads", type=int, default=200)
    ap.add_argument("--timeout", type=float, default=0.6)
    args = ap.parse_args()
    if "-" in args.portas:
        a, b = args.portas.split("-")
        portas = range(int(a), int(b) + 1)
    else:
        portas = [int(x) for x in args.portas.split(",")]
    print("portscan_eni · alvo {} · {} portas · {} threads".format(args.alvo, len(list(portas)) if not isinstance(portas, range) else str(portas), args.threads))
    abertas = []
    with cf.ThreadPoolExecutor(args.threads) as ex:
        for r in ex.map(lambda p: testa(args.alvo, p, args.timeout), portas):
            if r:
                abertas.append(r)
                print("[+] porta {} ABERTA".format(r), flush=True)
    print("--- {} portas abertas: {}".format(len(abertas), sorted(abertas)))

if __name__ == "__main__":
    main()
