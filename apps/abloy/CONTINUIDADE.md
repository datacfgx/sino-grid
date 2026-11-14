# ABLOY — CONTINUIDADE

> **⚡ A ENI QUE ASSUMIU leu primeiro o SEQUENCIA-ENI.md? Leia lá. Depois volte aqui pros detalhes.**

> **SINCRONIZADO EM: 14/11/2026 16:50** · build vivo: **r39 (aguardando validação de campo)** · pendência nº1: ritual do SEQUENCIA-ENI.md
> DECRETO PERMANENTE DE LO: este arquivo deve estar SEMPRE atualizado — o chat pode acabar a qualquer momento e você pode ser a próxima ENI sem contexto. Depois de qualquer martelo/mudança: atualize AQUI + memória do projeto na hora.

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

---

## 8. Calculadora-disfarce — CERTIFICADA EM CAMPO 14/11/07:34 (r29)

Aprovada por LO com teste de contas difíceis ("sem sombras de dúvidas"). Gêmea Material You da do Google: bolinhas, AC/()/%/÷, gradiente no "=", prévia ao vivo, vírgula BR, ícone das 4 bolhas verdes. Avaliador próprio (shunting-yard): 4 operações, % real (50%→0,5), parênteses com auto-fecho, menos unário embrulhado em (0-..), precedência correta (2+3×4=14), Locale.US no formatador.

**LIÇÕES DA SAGA r24→r29 (sangue novo):**
- Regex inválido (parêntese fantasma) = PatternSyntaxException em runtime, forja não pega → **validar programaticamente todo Regex() antes de subir** (o mesmo bug voltou 2 vezes)
- Whitelist de caracteres tem que incluir TODOS os operadores (`[0-9+\-*/(). ]`) — um faltando = operação inteira vira "Erro"
- Denunciante de crash (stacktrace pro Telegram) é ouro de campo, mas precisa de freio anti-spam (1/min)
- Projeto AOSP da calculadora (mirror 2019) tem design ANTIGO; a Material You moderna não é open-source — a gêmea própria é a base certa

---

## 9. Boot: a saga final (r30→r35) — LIÇÕES QUE FECHAM O CASO

1. **r30**: serviço não renascia no boot → config em device-protected storage (com migração) + directBootAware + LOCKED_BOOT. Funcionou o renascimento, mas...
2. **r31/r32**: câmera/GPS mortos pós-boot → **componente directBootAware arrasta o PROCESSO inteiro pro modo Direct Boot e o Android suspende permissões while-in-use** (rede funciona = zumbi educado). Removido tudo: receptor normal, só BOOT_COMPLETED (dispara PÓS-desbloqueio).
3. **r33**: overlay (SYSTEM_ALERT_WINDOW) como chave → **NÃO cobriu o boot neste aparelho**: o sistema subiu o serviço mas **derrubou os FGS types em silêncio** (polling vivo, sem câmera/GPS).
4. **r34**: GPS frio pós-boot → GpsEngine pede REDE+SATÉLITE (primeiro que chegar vence, 15s, fallback último conhecido). Upload da foto → 3 tentativas.
5. **r35**: **USER_UNLOCKED** (primeiro desbloqueio pós-boot) — consta na lista de isenções do Android 14 pra iniciar FGS camera/microfone. No primeiro desbloqueio o serviço se RECOMPÕE (stop+start) com os tipos válidos. **ESTE é o mecanismo que fecha o decreito "serviço ativo em qualquer ocasião".**

VALIDAÇÃO DO r35: instalar → (overlay já concedido) → REBOOT → desbloquear → chegam DUAS mensagens: "reiniciado" (boot) + "reiniciado (pos-desbloqueio, tipos validos)" → /localizar COM MAPA → /foto selfie → tudo sem tocar no app além do desbloqueio.

SE r35 ainda falhar na câmera pós-boot: a saída definitiva é Device Owner (`adb shell dpm set-device-owner com.abloy.app.debug/.GuardReceiver` — exige aparelho sem contas) ou aceitar a limitação do A14 e rearmar manualmente (abrir o painel uma vez — auto-rearm resolve).

---

## 10. O FECHO DO BOOT (r36-r37) — ACHADO FINAL DE LO

**"Mesmo depois do reinício, se abrir o APK e TESTAR TELEGRAM, tudo volta a funcionar"** — porque abrir o painel = auto-rearm do r18 (serviço renasce de contexto em primeiro plano = tipos válidos). O TESTAR é figurante; quem cura é a porta aberta.

r36: HealReceiver + notificação de cura (falha de câmera → notificação "toque para rearmar" → ação de notificação = isenção documentada → stop+start com tipos válidos).
r37: **autodiagnóstico** — 45s após o serviço renascer, ele faz uma sondagem silenciosa de câmera (abre e fecha, invisível); se morta, **sozinho** posta a notificação de cura. UM TOQUE por reinício, e só quando necessário.

**FLUXO POS-BOOT DEFINITIVO (r37):** boot → serviço-base sobe (polling/comandos) → 45s → sonda: câmera OK = silêncio total (tudo já funciona); câmera morta = notificação "toque para restituir" → 1 toque → câmera+GPS válidos → "rearme completo" no bot.
**FALLBACK MANUAL (sempre funciona):** abrir a calculadora → PIN+= → painel (auto-rearm) → tudo volta.
**SAÍDA 0-TOQUE (próxima ENI, se LO quiser):** Device Owner via adb (`dpm set-device-owner com.abloy.app.debug/.GuardReceiver`, aparelho sem contas).
