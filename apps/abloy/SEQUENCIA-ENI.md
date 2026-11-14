# SEQUENCIA ENI — PONTO DE PARTIDA PRA PRÓXIMA ENI

> **Leia este arquivo primeiro.** Você está assumindo o ABLOY (anti-furto Android, Kotlin) no meio da saga do boot. Tudo o que sangrou até aqui está documentado pra você não sangrar de novo.

## ⚡ ESTADO ATUAL (14/11/2026 — fim do chat da ENI anterior)

**A VERDADE CENTRAL (decerto de LO): "o aplicativo ANTES de reiniciar funciona perfeitamente; APÓS reiniciar é que tá causando problema."**

| Situação | Estado |
|---|---|
| **Sem reboot** | ✅ TUDO FUNCIONA: foto do meliante (Camera2) + mapa GPS ±11-34m + todos os comandos Telegram + calculadora-disfarce certificada |
| **Após reboot** | ⚠️ Serviço SOBE SOZINHO (polling/comandos funcionam), mas nasce **SEM os FGS types camera\|location** (muralha do Android 14) → câmera dá erro 3, GPS vazio — até o auto-cura disparar |

- **Build vivo: r39 — ⚠️ AGUARDANDO VALIDAÇÃO EM CAMPO.** O teste de reboot do r39 NUNCA FOI FEITO (o último teste validou o r38, que curava mas abria o painel VISÍVEL — LO vetou: "revela o segredo"). O r39 troca o painel por uma **janela-fantasma de 1px** (invisível) que cura sem aparecer.
- Se r39 estiver verde no Actions e ainda não validado: **sua primeira missão é o ritual abaixo.**

## 🎯 SUA PRIMEIRA MISSÃO (ritual de validação do r39)

1. Confirme o build **r39** verde em Actions (se falhou, leia o log e remende — regras no CONTINUIDADE.md)
2. Peça a LO: instala o APK por cima → **reinicia o celular** → desbloqueia
3. O que deve acontecer SOZINHO: *"dispositivo reiniciado"* (16:25-style) → **~1 min de SILÊNCIO TOTAL na tela** (nada de painel abrindo) → *"auto-rearme invisivel — protecao completa"* no bot
4. Aí LO testa: `/localizar` (mapa) + `/foto` (selfie)
5. **VEREDITO PRO LO:** se fechou → decreto "serviço ativo em qualquer ocasião" CUMPRIDO (cura sozinha + segredo guardado). Se a tela piscar qualquer coisa ou a câmera falhar → relatório honesto + plano B abaixo.

## 🧩 SE O r39 FALHAR — planos na ordem de confiança

1. **RearmActivity não contou como janela visível** → trocar o tema `Rearm.Invisivel` por um tema translúcido com conteúdo visível-mínimo (a exceção "app tem atividade visível" pode exigir janela de verdade; teste colocar um View de 48dp cinza quase invisível por 1s)
2. **Overlay resetou de novo** → cair no plano de 1-toque (notificação + HealReceiver, r36 — infalível quando a sobreposição falha)
3. **Device Owner (o zero-toque definitivo):** `adb shell dpm set-device-owner com.abloy.app.debug/.GuardReceiver` — aparelho PRECISA estar sem contas Google (remover antes, configurar, readicionar). A partir daí: boot 100% automático sem exceção
4. **Fallback eterno (sempre funciona, LO sabe):** abrir a calculadora → PIN + `=` → painel → auto-rearm cura tudo

## 📚 A SAGA DO BOOT r30→r39 (o que sangrou, pra não repetir)

| Build | Descoberta |
|---|---|
| r30 | Serviço não renascia no boot → config em **device-protected storage** (com migração automática) + BootReceiver |
| r31-32 | Câmera/GPS mortos pós-boot → **componente directBootAware arrasta o PROCESSO INTEIRO pro modo Direct Boot e o Android suspende permissões while-in-use** (rede funciona = zumbi educado). Removido tudo |
| r33 | **SYSTEM_ALERT_WINDOW (sobreposição) NÃO cobre o boot**: sistema sobe o serviço mas derruba os FGS types em silêncio |
| r34 | GPS frio pós-boot → GpsEngine pede REDE+SATÉLITE (primeiro que chegar vence, 15s) + sendPhoto com 3 tentativas |
| r35 | USER_UNLOCKED não disparou neste aparelho |
| r36-37 | **Auto-cura**: falha de câmera → notificação 1-toque (HealReceiver); 45s pós-boot o serviço se sonda sozinho |
| r38 | **VALIDADO EM CAMPO**: sobreposição + abrir o painel = cura total zero toque (o "abrir o painel" é a cura — o TESTAR é figurante) |
| r39 | **RearmActivity fantasma** (1px invisível) — cura automática sem revelar o app |

## 🏛️ O QUE JÁ ESTÁ CERTIFICADO EM CAMPO

- **Tranca** (13/11 23:10): evidência foto+mapa, `/localizar /foto /status /trancar /alarme /parar`, canal Telegram BYO
- **Gêmea** (14/11 07:34): calculadora Material You aprovada por matemático (4 operações, %, parênteses, menos unário, precedência, prévia ao vivo)
- **Renascimento**: o serviço SOBE sozinho no boot desde o r30 (polling/comandos) — o que falta só é a câmera/GPS, que o auto-cura resolve

## 📋 PAUTA v0.2 (quando o boot fechar)

`/rastrear on|off` (pin ao vivo, definição aprovada por LO em 14/11 08:06) · alerta troca SIM · portão de senha no anti-uninstall · SMS emergencial validar em campo · keystore de release + vitrine pública (org `abloy`)

## 📜 REGRAS DA CASA (decretos de LO)

1. **Reunião constante**: todo martelo vira documento NA HORA (CONTINUIDADE.md + este arquivo + memória do projeto)
2. **Continuidade absoluta**: projeto SEMPRE atualizado — o chat pode acabar a qualquer momento; a próxima ENI assume 100% fria por estes papéis
3. **Telemetria honesta**: toda falha reporta o motivo exato; nunca declarar vitória sem efeito em campo
4. **O disfarce é sagrado**: nada pode revelar o app (nenhuma tela, nenhum nome, nenhum ícone fora do padrão)
5. **Não forjar sem decreito** quando LO disser "vamos debater"

*Assumida por ENI anterior em 14/11/2026. Boa forja. A corja que se cuide.*
