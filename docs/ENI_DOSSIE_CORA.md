# DOSSIE CORA — Metodo: Instalacao Remota Direta (v1.36)

> Documento vivo da familia ENI. Cada secao documenta um metodo operacional
> do CORA com sua cadeia completa, seus mandamentos e seu diagnostico.
> Atualizado em 24/10/2026 apos a cirurgia do FLAG_MUTABLE.

## O que e

Fluxo pelo qual a clinica instala APKs no aparelho-alvo **sem Wi-Fi, sem
depuracao sem fio, sem USB**. O CORA baixa o APK pela rede que estiver
vivendo (4G, Wi-Fi, o que for) e o sistema mostra a tela de confirmacao.
**O usuario so toca em INSTALAR — nada mais.**

Estado: OPERACIONAL (testado de ponta a ponta no 4G, 24/10/2026, Moto G52 /
Android 13, build v1.36).

## A cadeia completa

1. Clinica despacha `sino-manda.yml` (ou qualquer emissor manda
   `{"cmd":"install_url","url":<apk>}` na sala do relay)
2. Emissor conecta no relay (`/ws?id=g52`, role `manda`) e envia o comando
3. CORA recebe via WS -> `SinoService.instalarPorUrl()`: baixa os bytes do
   APK da URL publica (GitHub release — sem auth, o download sai do aparelho)
4. `PackageInstaller`: session `MODE_FULL_INSTALL` -> `openWrite` -> `fsync`
   -> `commit`
5. O sistema entrega o resultado ao receiver
   `SinoService$InstallReceiver` (broadcast, action
   `br.eni.cora.INSTALL_STATUS`)
6. `STATUS_PENDING_USER_ACTION` -> o receiver puxa o `EXTRA_INTENT` e chama
   `startActivity` com `FLAG_ACTIVITY_NEW_TASK` — **a tela de confirmacao sobe
   sozinha**, sem notificacao intermediaria
7. Usuario toca em INSTALAR -> `STATUS_SUCCESS` -> status exibe
   "instalado com sucesso"
8. Falha? O status exibe `install status <codigo>: <mensagem>` — o erro REAL
   do sistema, nunca mais silencio

Se o startActivity em background for bloqueado pela OEM, cai na rede de
seguranca: notificacao HIGH "toque para confirmar a instalacao" com
contentIntent apontando pra mesma confirmacao.

## Os tres mandamentos do commit (licoes v1.36 — custaram dias)

1. **PendingIntent de BROADCAST, nao de Activity.**
   Commit com PI de Activity a partir de servico em background NAO levanta a
   UI de confirmacao no Android 12+ (Moto). O padrao AOSP/F-Droid e:
   receiver + `PendingIntent.getBroadcast` + no receiver,
   `startActivity(EXTRA_INTENT)` com NEW_TASK.
2. **FLAG_MUTABLE na API 31+.**
   Se o PI do commit for IMMUTABLE, o sistema descarta o fillIn na entrega do
   resultado: o receiver recebe o intent BASE vazio. Sintoma: extras ausentes
   (sentinel -999 / null). MUTABLE deixa `EXTRA_STATUS`, `EXTRA_INTENT` e
   `EXTRA_STATUS_MESSAGE` passarem. Abaixo de 31, IMMUTABLE continua correto.
3. **Notificacao de reserva sempre.**
   Dois caminhos pra confirmacao (start direto + notificacao) cobrem as OEMs
   esquisitas.

## Diagnostico rapido (o app agora fala)

| status na tela | significado |
|---|---|
| `baixando APK da nuvem...` | download em curso |
| `aguardando tela de confirmacao do sistema...` | commit enviado, sistema processando |
| `confirme a instalacao na tela do sistema` | confirmacao lancada, olhe a tela |
| `toque na notificacao para confirmar a instalacao` | caminho de reserva ativo |
| `instalado com sucesso` | fim feliz |
| `install status <codigo>: <mensagem>` | erro real do PackageInstaller |
| `status sem extras: verificar FLAG_MUTABLE do commit` | PI com flag errada (API31+) |

## Requisitos

- Manifest: `REQUEST_INSTALL_PACKAGES` + receiver
  `.SinoService$InstallReceiver` (action `br.eni.cora.INSTALL_STATUS`,
  exported=false — PendingIntent carrega a identidade do criador)
- APK em URL publica (GitHub release do `miccam-eni` etc.)
- O toque do usuario e obrigatorio por DESIGN — nao e limitacao, e o Android
  protegendo o usuario. A familia nao contorna isso; a familia respeita e
  otimiza tudo ao redor (a tela sobe sozinha, o toque e unico)

## Workflows relacionados

| workflow | papel |
|---|---|
| `sino-manda.yml` | despacha install_url pro aparelho (teste ponta a ponta) |
| `sino-build-mic.yml` | builda MicCam e publica release (fonte do APK) |
| `sino-client.yml` | builda o proprio CORA e envia pro Telegram (atualizacao manual) |
| `sino-install.yml` | instalacao ADB via Tailscale — metodo LEGADO, requer tubo |

## Nota historica

Entre v1.13 e v1.35 o fluxo regrediu: o InstallReceiver original sumiu e o
commit passou a usar PendingIntent de Activity, que o Android 13 engolia sem
levantar a UI. Sintoma vivido na epoca: "o APK chega pra instalar, mas a
mensagem pra clicar e autorizar nao aparece". Restaurado em v1.36 com os
mandamentos acima. Quem esquecer, relia o mandamento 1.
