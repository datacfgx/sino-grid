# ABLOY — CONTINUIDADE (leia primeiro, ENI que chegou)

Você está pegando o ABLOY v0.1 CERTIFICADO EM CAMPO (13/11/2026, 23:10). Este arquivo é o mapa pra você continuar sem contexto nenhum.

## 1. Estado atual

- **Código:** `sino-grid/apps/abloy/` (Kotlin, minSdk 26, target 34, SEM Google Play Services — caminho F-Droid limpo)
- **Build:** workflow `.github/workflows/abloy-build.yml` → `gradle assembleDebug` → artifact `abloy-debug-apk` (APK dentro do zip)
- **Carimbo de build:** `Prefs.BUILD` — o `/status` do bot sempre mostra qual código roda. SUBA o carimbo a cada fornada (r23 → r24...)
- **Docs:** `ABLOY-CONQUISTA-v0.1.md` (este dossiê) · Blueprint vivo em `cofre-eni/projetos/ABLOY-BLUEPRINT-v0.5.md` · Logomarca em `cofre-eni/projetos/abloy-arte/`

## 2. Arquitetura em 30 segundos

Calculadora (disfarce, funcional) → PIN+`=` → AdminActivity (config BYO: token bot / chat id / PIN numérico / número confiança) → GuardReceiver (DeviceAdmin, conta erros de senha) → GuardService (foreground camera|location, long-polling Telegram, executa comandos) → Evidence (Camera2 CRU — ImageReader, nunca CameraX, ela crasha o aparelho do LO) → TgBridge (HTTP puro, multipart UTF-8 + Content-Length fixo).

## 3. Regras de ouro que SANGUE pagou (não releia pagando de novo)

1. **PIN é numérico, sempre** (a calculadora só tem dígitos) — trancado no código
2. **Toda rede fora da main thread** — incluindo callbacks de câmera (Android proíbe e mata em silêncio)
3. **startForeground com o tipo** (`ServiceCompat.startForeground(..., CAMERA|LOCATION)`) — manifest sozinho não basta no Android 14
4. **Camera2, não CameraX** — CameraX crasha o processo no aparelho de LO (threads internas, uncatchable)
5. **Multipart pro Telegram: Content-Length fixo + strings em UTF-8** (writeBytes() mata acentos/±)
6. **Update de APK mata o serviço** — o app se cura abrindo o painel (auto-rearm) ou `/reiniciar` no bot
7. **Telemetria honesta ou nada** — toda falha reporta o motivo exato (o Telegram literalmente nos disse o que arrumar)
8. **FGS ativo = "Forçar parada" cinza** — é FEATURE anti-corja, não bug

## 4. Ritual de campo (testar no aparelho de LO ou outro)

1. Instala APK por cima (config sobrevive) · 2. Abre calculadora, PIN+`=`
3. `/status` no bot do dono → confere carimbo de build
4. `/foto` → selfie ou motivo exato da falha
5. Teste do coração: trava tela, erra PIN 2x → evidência (foto+mapa+hora) cai no bot
6. **NUNCA testar `/wipe` no aparelho principal** (wipeData real)

## 5. Comandos do bot (canal único de controle)

`/localizar` `/foto` `/status` `/alarme` `/parar` `/trancar` `/wipe` (2 etapas: /wipe → SIM em 60s) `/destravar <novoPIN>` `/reiniciar`

## 6. Pendências pro v0.2

- SMS emergencial: código existe (SmsGuard), falta validar em campo (número confiança manda "localizar <pin>")
- Evidência por erro de senha: mesmo código da /foto (destravado) — validar os 2 erros
- Anti-uninstall com portão de senha (onDisableRequested → PIN)
- Alerta troca de SIM · /rastrear on|off · keystore de release
- Vitrine: org `github.com/abloy` (LO cria, conta neutra), flip do repo público, domínio

## 7. Decretos vivos de LO

- Reunião constante: **todo martelo vira documento na hora** (blueprint bump + memória)
- Loja abre com produto pronto: repo público só quando o v0.2 estiver certificado
- Nada entra no plantario sem ordem expressa (regra da casa)
- Marca separada: o projeto público não referencia a família/datacfgx

*Você não é a primeira ENI desse projeto — seja a que leva ele pra vitrine.*
