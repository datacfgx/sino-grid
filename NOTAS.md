# NOTAS OPERACIONAIS DO SINO

Saberes de campo, colhidos ao vivo. Leia antes de operar.

## Navegacao
- **/shell abre SEMPRE no Firefox** (Android). O Chrome trava na tela preta do ttyd
  (WebSocket nao conecta/cache). Firefox resolve de primeira com http://100.x:PORTA.
- URL sempre com http:// - se o navegador "corrigir" pra https, volta manual.

## Colagem no RDP (Windows App)
- A area de transferencia mastiga textos longos. Coladas curtas (uma linha), uma por vez.
- Linhas quebradas geram prompt >> preso - Ctrl+C pra escapar.

## Colagem no Telegram (/cmd /win)
- Comandos com aspas/asparos altos: o heredoc do workflow protege, mas digite com atencao.
- Sempre com o prefixo: /cmd (Kali), /win (Windows). Mensagem sem prefixo = ignorada.

## Sessoes
- /rdp e /shell vivem ~6h (teto do Actions). /rdp renasce com /rdp, shell com /shell.
- GH-WIN e GH-KALI (cmd/win) rodam no repo PRIVADO: ~2000 min/mes. Se as corridas
  falharem no provisioning sem executar nada = cota esgotada, espera o reset do ciclo.

## Uber (uber-cli)
- Windows do Actions: rodar tools/patch-uber.ps1 (iex (irm ...)) apos npm i -g -
  o spawn sem shell:true nao acha o agent-browser (shim .cmd do npm).
- Sessao nao persiste entre maquinas - reidratar e o proximo tijolo (Rota B).
- Rota definitiva: uber-bridge no notebook da casa (Tailscale), ainda nao forjado.

## OSINT
- phoneinfoga: `phoneinfoga scan -n +55DDDNUMERO` (zero config).
- ghunt: precisa `ghunt login` uma vez (cookies Google) - faca em PC, nao no celular.

## Trem
- /trem = API Próximo Trem ViaMobilidade. Codigos: JSI (Jardim Silveira), IPV (Itapevi).
- [plataforma] = trem parado na estacao. [deslocamento] = a caminho.
