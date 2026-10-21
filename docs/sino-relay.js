// sino-relay v2 — SEM Durable Object (estado global do isolate)
// Uso: 1 aparelho + 1 runner por vez (o nosso caso). Se os dois sockets
// cairem em isolates diferentes, o cliente reconecta ate achar a sala.
const rooms = new Map();

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    if (url.pathname === "/health") return new Response("ok");
    if (url.pathname === "/peek") {
      const id = (url.searchParams.get("id") || "g52").slice(0, 64);
      return new Response(JSON.stringify({ id, peers: rooms.get(id) ? rooms.get(id).size : 0 }));
    }
    if (url.pathname !== "/ws") return new Response("sino-relay: /ws?id=<device> | /peek?id=<device>");
    if (request.headers.get("Upgrade") !== "websocket") return new Response("ws only", { status: 426 });
    const id = (url.searchParams.get("id") || "g52").slice(0, 64);
    const pair = new WebSocketPair();
    const [client, server] = Object.values(pair);
    server.accept();
    server.role = request.headers.get("X-Role") || "anon";
    if (!rooms.has(id)) rooms.set(id, new Set());
    const room = rooms.get(id);
    room.add(server);
    server.addEventListener("message", (ev) => {
      for (const s of room) if (s !== server && s.readyState === 1) s.send(ev.data);
    });
    const drop = () => { room.delete(server); if (room.size === 0) rooms.delete(id); };
    server.addEventListener("close", drop);
    server.addEventListener("error", drop);
    server.send(JSON.stringify({ type: "joined", role: server.role, peers: room.size }));
    return new Response(null, { status: 101, webSocket: client });
  }
};
