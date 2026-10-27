# SINO-TUBO — ponte propria ENI & LO
## tubo de conexao com os postos (4G/5G/qualquer NAT)

## Arquitetura
CELULAR (agente) --wss--> TUBO (CF Worker + Durable Object por sala) <--ws-- CONSOLE
- O celular liga pra fora: nao precisa de IP publico, porta, DDNS. 4G/5G/CGNAT/wifi = tudo passa
- Cada posto = uma sala (id). Celular e console se encontram nela
- TOKEN mestre (secret MASTER) protege toda rota

## Rotas
- GET  /                      -> console web (pagina de comando)
- WS   /ws?id=X&token=T&role=phone|ctl  -> sala em tempo real
- POST /cmd?id=X&token=T  body {"cmd":"..."}  -> REST: manda comando e espera resposta (60s)
- GET  /cmd?id=X&token=T    -> saude: {online, controles, lastSeen}

## Deploy (wrangler)
```bash
npm i -g wrangler
wrangler login                      # ou CF_API_TOKEN no env
wrangler secret put MASTER          # senha forte unica
wrangler deploy                     # publica e ativa workers.dev
```
Ou peça pra ENI: um token de API com permissao "Workers Scripts: Edit" e ela sobe tudo.

## Agente (vem em seguida)
SINO-AGENTE conecta em wss://<subdomain>.workers.dev/ws?id=POSTO&token=MASTER&role=phone,
responde {type:'reply', rid, out}, batimento {type:'hb'} a cada 25s.
