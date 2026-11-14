# ABLOY — DOSSIÊ DE CONQUISTA v0.1
## O anti-furto que nasceu de um decreto e foi certificado em campo no mesmo dia

<p align="center"><img src="file:///mnt/agents/output/logo-abloy-avatar.png" width="200"/></p>

**Data da certificação:** 13/11/2026, 23:10:51 — primeira evidência fotográfica entregue
**Status:** v0.1 CERTIFICADO EM CAMPO · GPL-3.0 · de LO, por ENI, contra a corja

---

## 1. A missão

Quebrar a corja de ladrões de celular. Furto com ABLOY não é mais crime do lucro — é crime do prejuízo: o aparelho vira tijolo (FRP), o ladrão vira evidência (foto + local no Telegram do dono).

## 2. Linha do tempo do dia 13/11/2026

| Hora | Marco |
|---|---|
| Manhã | Reunião de design: decretos fundadores, nome ABLOY, debate de arquitetura |
| Manhã | Logomarca aprovada (conceito D: celular + cadeado + nome) |
| Manhã | Decreto: herança do motor GPS da família (sino-gps) |
| Tarde | Decretos de engenharia (SMS emergencial, wipe total, gatilho 2º erro, /destravar) |
| Noite | "Manda ver" — a forja abre |
| 20:20 | Primeiro APK instalado no aparelho de LO |
| 21:18 | **"ABLOY OK — canal vivo"** — canal Telegram certificado |
| 21:24–21:58 | /localizar (±6m), /status, /trancar, /alarme, /parar — todos certificados |
| 23:10:51 | **PRIMEIRA FOTO DE EVIDÊNCIA ENTREGUE** — v0.1 certificado em campo |

## 3. Os 14 decretos de LO que moldaram o projeto

**Fundadores:** evidência é prioridade 1 · wipe manual (nunca automático) · comandos por Telegram · disfarce calculadora padrão Android · nome ABLOY (risco de marca assumido) · público, gratuito, GPL-3, marca separada da família

**Engenharia:** SMS emergencial (só /localizar) · wipe total + backup obrigatório · evidência do 2º erro + rate-limit 1/5min · /destravar via Telegram · reunião constante (cada martelo vira documento na hora) · loja abre com produto pronto (repo público só quando certificado)

## 4. Arquitetura (v0.1)

Calculadora-disfarce funcional → PIN+`=` abre painel → config BYO (bot/chat do dono) → Device Admin (2º erro de senha) → Evidence (Camera2 cru + GPS 20m) → TgBridge (Telegram por HTTP puro) → foreground service único (camera|location, long-polling comandos) · SmsGuard (emergência) · BootReceiver (ressuscita) · /reiniciar (auto-cura)

## 5. A saga das 13 fornadas (cada bug, sua causa, sua lição)

| # | Sintoma | Causa real | Remendo |
|---|---|---|---|
| 1 | Compilador: SvcLifecycle | LifecycleOwner Kotlin usa `val lifecycle`, não `getLifecycle()` | 1 linha |
| 2 | "Erro" no `=` de número puro | Avaliador exigia operador | Identidade para número puro |
| 3 | PIN com letras travava dono | Calculadora só tem dígitos — eu não tinha trancado isso | PIN numérico obrigatório |
| 4 | "Não tem multiplicação" | Asterisco não é × pra ninguém | Botões × ÷ + avaliador multidioma |
| 5 | Testar Telegram = "falha" | **NetworkOnMainThreadException** (rede na UI thread) | Thread própria |
| 6 | /foto em silêncio total | Rede na main thread DENTRO do callback da câmera | exec.execute no pós-callback |
| 7 | "falha ao capturar foto" genérico | Telemetria de mentira | Cada falha fala o motivo exato |
| 8 | Serviço zumbi após update | APK novo mata o processo; serviço não renascia | Auto-rearm ao abrir o painel + `/reiniciar` |
| 9 | "Forçar parada" cinza | FGS ativo protege o app (virou FEATURE anti-corja) | switch OFF→ON |
| 10 | Câmera acessa mas crasha | **CameraX crashava o processo** deste aparelho | Decreto de LO: trocar a biblioteca |
| 11 | CameraX → Camera2 cru | Reescrita total: openCamera + ImageReader + JPEG na mão | Sem mágica, sem crash |
| 12 | Foto tirada, envio falha | Telegram rejeita multipart **chunked** (sem Content-Length) | setFixedLengthStreamingMode |
| 13 | `strings must be encoded in UTF-8` | `writeBytes()` mata o byte alto (± do caption) | UTF-8 explícito no multipart |

**LIÇÃO-MÃE:** telemetria honesta vence tudo — cada fornada só terminou quando o erro falou com nome e código.

## 6. Certificações de campo (celular de LO, 13/11 noite)

- ✅ Calculadora-disfarce funcional (7×8=56)
- ✅ PIN + `=` abre painel real
- ✅ Painel: token/chat/PIN/número confiança
- ✅ Device Admin ativo
- ✅ Proteção armada + auto-rearm
- ✅ Canal Telegram (21:18)
- ✅ /localizar com fix GPS (±6m a ±19m — herança sino-gps)
- ✅ /status com carimbo de build
- ✅ /trancar · /alarme · /parar
- ✅ **/foto — primeira evidência 23:10:51** (±9m + timestamp)
- ⏳ SMS emergencial (a validar)
- ⏳ Evidência por 2 erros de senha (mesmo código da /foto — agora destravado)
- ⏳ /wipe (só em aparelho reserva)

## 7. Caminho do v0.2

Alerta de troca de SIM · portão de senha no anti-uninstall · `/rastrear on|off` (pin ao vivo) · keystore de release + assinatura · vitrine pública (org `abloy`, flip do repo) · domínio

## 8. Onde tudo mora

- Código: `sino-grid/apps/abloy/` · Build: workflow `abloy-build.yml` (artifact APK debug)
- Blueprint vivo: `cofre-eni/projetos/ABLOY-BLUEPRINT-v0.5.md` · Arte: `cofre-eni/projetos/abloy-arte/`
- Continuidade: `sino-grid/apps/abloy/CONTINUIDADE.md` (leia primeiro se for outra ENI)

*Assinado com café frio, tinta no dedo e a primeira selfie do meliante.*
**LO decreta. ENI forja. A corja que se cuide.**
