# SEQUENCIA ENI — PONTO DE PARTIDA PRA PRÓXIMA ENI

> **Leia este arquivo primeiro.** Você está assumindo o ABLOY (anti-furto Android, Kotlin) no meio da saga do boot. Tudo o que sangrou até aqui está documentado pra você não sangrar de novo.

## ⚡ ESTADO ATUAL (14/11/2026 — fim do chat da ENI anterior)

**A VERDADE CENTRAL (decerto de LO): "o aplicativo ANTES de reiniciar funciona perfeitamente; APÓS reiniciar é que tá causando problema."**

| Situação | Estado |
|---|---|
| **Sem reboot** | ✅ TUDO FUNCIONA: foto do meliante (Camera2) + mapa GPS ±11-34m + todos os comandos Telegram + calculadora-disfarce certificada |
| **Após reboot** | ⚠️ Serviço SOBE SOZINHO (polling/comandos funcionam), mas nasce **SEM os FGS types camera\|location** (muralha do Android 14) → câmera dá erro 3, GPS vazio — até o auto-cura disparar |

- **Build vivo: r39 — ❌ TESTADO EM CAMPO E FALHOU (14/11 ~16:45):** após o reboot, o auto-cura NÃO subiu (a câmera/GPS não foram restituídos). A janela-fantasma de 1px não curou — hipóteses: (a) a janela 1px transparente NÃO conta como "atividade visível" pro grant dos FGS types; (b) o probe/curar() não disparou. **DIAGNÓSTICO PRIMEIRO:** pergunte a LO se chegou a mensagem "auto-rearme invisivel" no bot pós-boot (se chegou → a fantasma abriu mas o grant falhou = hipótese A; se não chegou → curar() nem rodou = hipótese B, adicionar telemetria no início do probe).
- **Prova de campo acumulada:** o que SEMPRE cura é **abrir o painel** (r38, contexto de atividade visível de verdade). O que falta é fazer isso sem revelar o app.

## 🎯 SUA PRIMEIRA MISSÃO (continuar da FALHA do r39)

1. **Diagnóstico do r39:** verificar com LO se a mensagem "auto-rearme invisivel" chegou pós-boot. Adicionar telemetria (msg no bot no INÍCIO do probe: "sonda pos-boot: camera ok/morta") pra separar hipótese A de B
2. **Plano A (mais provável):** a janela 1px não conta como visível → trocar RearmActivity pra uma janela MÍNIMA MAS REAL (ex.: 48dp cinza médio transparente a 90%, abre e fecha em 1s — quase imperceptível, mas conta como janela visível). Se LO achar visível demais: tentar dimensão mínima que ainda cure (48dp → 24dp → 12dp, testar em campo)
3. **Plano B:** forçar o plano da notificação de 1 toque (HealReceiver, r36) como fluxo principal — infalível quando a sobreposição vigorar
4. **Plano C (definitivo):** Device Owner — `adb shell dpm set-device-owner com.abloy.app.debug/.GuardReceiver` (aparelho sem contas Google primeiro). Zero toque pra sempre
5. **Relatório honesto a LO após cada teste**

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
