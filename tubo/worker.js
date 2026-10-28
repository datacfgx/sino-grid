// SINO-TUBO v1.7 — padrao comprovado (server.accept + listeners), zero hibernacao
function json(o, status = 200) {
  return new Response(JSON.stringify(o), { status, headers: { 'Content-Type': 'application/json' } });
}

export class Cano {
  constructor(state) { this.state = state; this.phones = new Set(); this.ctls = new Set(); this.pending = new Map(); this.seq = 0; this.lastSeen = 0; }
  sendTo(set, data) { for (const s of set) { try { s.send(data); } catch (e) { set.delete(s); } } }
  async fetch(request) {
    const url = new URL(request.url);
    if (url.pathname === '/ws') {
      const role = url.searchParams.get('role') === 'phone' ? 'phone' : 'ctl';
      const pair = new WebSocketPair();
      const [client, server] = Object.values(pair);
      server.accept();
      const set = role === 'phone' ? this.phones : this.ctls;
      set.add(server);
      if (role === 'phone') {
        this.lastSeen = Date.now();
        this.sendTo(this.ctls, JSON.stringify({ type: 'presence', online: true }));
      } else {
        server.send(JSON.stringify({ type: 'presence', online: this.phones.size > 0 }));
      }
      server.addEventListener('close', () => {
        set.delete(server);
        if (role === 'phone') this.sendTo(this.ctls, JSON.stringify({ type: 'presence', online: this.phones.size > 0 }));
      });
      server.addEventListener('error', () => set.delete(server));
      server.addEventListener('json', (e) => this.onMsg(server, role, e.data));
      return new Response(null, { status: 101, webSocket: client });
    }
    if (url.pathname === '/cmd' && request.method === 'POST') {
      if (this.phones.size === 0) return json({ erro: 'sem telefone na sala' }, 503);
      const body = await request.json().catch(() => ({}));
      const cmd = String(body.cmd || '');
      if (!cmd) return json({ erro: 'cmd vazio' }, 400);
      const rid = ++this.seq;
      return new Promise((resolve) => {
        const to = setTimeout(() => { this.pending.delete(rid); resolve(json({ erro: 'timeout 60s' }, 504)); }, 60000);
        this.pending.set(rid, { resolve, to });
        this.sendTo(this.phones, JSON.stringify({ type: 'cmd', rid, cmd }));
      });
    }
    if (url.pathname === '/cmd' && request.method === 'GET') {
      return json({ ok: true, online: this.phones.size > 0, controles: this.ctls.size, lastSeen: this.lastSeen });
    }
    return json({ ok: true, tubo: 'vivo v1.7' });
  }
  onMsg(server, role, data) {
    let obj;
    try { obj = JSON.parse(data); } catch (e) { obj = null; }
    if (obj && obj.type === 'hb') {
      this.lastSeen = Date.now();
      try { server.send(JSON.stringify({ type: 'hb' })); } catch (e) {}
      return;
    }
    if (obj && obj.type === 'reply' && obj.rid) {
      const p = this.pending.get(obj.rid);
      if (p) {
        clearTimeout(p.to); this.pending.delete(obj.rid);
        p.resolve(json({ out: String(obj.out == null ? '' : obj.out) }));
        return;
      }
      this.sendTo(this.ctls, data);
      return;
    }
    this.sendTo(role === 'phone' ? this.ctls : this.phones, data);
  }
}

const HTML = `<!doctype html><html lang="pt-BR"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>SINO-TUBO</title>
<style>
body{background:#0d1117;color:#c9d1d9;font:14px monospace;margin:0;height:100vh;display:flex;flex-direction:column}
header{padding:10px 14px;background:#161b22;display:flex;gap:8px;align-items:center;flex-wrap:wrap}
input,button{background:#21262d;color:#c9d1d9;border:1px solid #30363d;border-radius:6px;padding:7px 10px;font:14px monospace}
button{cursor:pointer}#dot{width:10px;height:10px;border-radius:50%;background:#f85149;display:inline-block}
#dot.on{background:#3fb950}
#out{flex:1;overflow:auto;padding:12px;white-space:pre-wrap}
#cmdline{display:flex;gap:8px;padding:10px;border-top:1px solid #30363d}
#cmd{flex:1}
.b{color:#58a6ff}.g{color:#3fb950}.r{color:#f85149}
</style></head><body>
<header>
<b class="b">SINO-TUBO</b>
<span id="dot"></span><span id="st">offline</span>
<input id="id" placeholder="posto" size="8">
<input id="tk" placeholder="token" type="password" size="14">
<button id="go">conectar</button>
</header>
<div id="out"></div>
<div id="cmdline"><span style="align-self:center">$</span><input id="cmd" placeholder="comando no telefone…" disabled></div>
<script>
const $ = (x) => document.getElementById(x);
const out = $('out');
function log(t, c) { const d = document.createElement('div'); if (c) d.className = c; d.textContent = t; out.appendChild(d); out.scrollTop = out.scrollHeight; }
$('id').value = localStorage.getItem('tubo_id') || 'posto1';
$('tk').value = localStorage.getItem('tubo_tk') || '';
let ws, rid = 0;
function setDot(on) { $('dot').className = on ? 'on' : ''; $('st').textContent = on ? 'telefone online' : 'offline'; }
function connect() {
  if (ws && ws.readyState <= 1) { try { ws.close(); } catch (e) {} }
  const id = $('id').value.trim(), tk = $('tk').value.trim();
  localStorage.setItem('tubo_id', id); localStorage.setItem('tubo_tk', tk);
  const proto = location.protocol === 'https:' ? 'wss' : 'ws';
  ws = new WebSocket(proto + '://' + location.host + '/ws?id=' + encodeURIComponent(id) + '&token=' + encodeURIComponent(tk) + '&role=ctl');
  ws.onopen = () => { log('· conectado ao tubo', 'b'); $('cmd').disabled = false; $('cmd').focus(); };
  ws.onclose = () => { setDot(false); $('cmd').disabled = true; log('· tubo caiu — reconectando em 3s', 'r'); setTimeout(connect, 3000); };
  ws.onerror = () => log('· erro de conexao', 'r');
  ws.onmessage = (e) => {
    let o; try { o = JSON.parse(e.data); } catch (ev) { o = null; }
    if (o && o.type === 'presence') { setDot(!!o.online); log(o.online ? '· telefone entrou na sala' : '· telefone saiu', o.online ? 'g' : 'r'); return; }
    if (o && o.type === 'reply') { log(o.out); return; }
    log(e.data);
  };
}
$('go').onclick = connect;
$('cmd').onkeydown = (e) => {
  if (e.key !== 'Enter' || !ws || ws.readyState !== 1) return;
  const c = $('cmd').value; if (!c.trim()) return;
  log('$ ' + c, 'b'); $('cmd').value = '';
  ws.send(JSON.stringify({ type: 'cmd', rid: ++rid, cmd: c }));
};
</script></body></html>`;

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    if (url.pathname === '/') {
      return new Response(HTML, { headers: { 'Content-Type': 'text/html; charset=utf-8' } });
    }
    if (url.pathname === '/ws' || url.pathname === '/cmd') {
      const id = url.searchParams.get('id') || 'posto1';
      const token = url.searchParams.get('token') || '';
      if (!env.MASTER || token !== env.MASTER) return json({ erro: 'token ruim' }, 403);
      const room = env.ROOMS.get(env.ROOMS.idFromName(id));
      return room.fetch(request);
    }
    return json({ erro: 'rota nao existe' }, 404);
  }
};
