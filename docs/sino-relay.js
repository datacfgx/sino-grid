// sino-relay — WebSocket room relay: telefone <-> runner
// DEPLOY (dash.cloudflare.com): Create Worker "sino-relay" → paste → Deploy.
// Depois: Settings > Variables > Durable Object Bindings > Add:
//   Variable name: ROOM | Durable Object namespace: criar "RoomNS" (class name: Room)
//   salvar + Deploy de novo. URL final: https://sino-relay.<subdomain>.workers.dev
export class Room {
  constructor(state, env) { this.state = state; this.sockets = new Set(); }
  async fetch(request) {
    if (request.headers.get("Upgrade") !== "websocket") return new Response("ws only", { status: 426 });
    const pair = new WebSocketPair();
    const [client, server] = Object.values(pair);
    server.accept();
    server.role = request.headers.get("X-Role") || "anon";
    this.sockets.add(server);
    server.addEventListener("message", (ev) => {
      for (const s of this.sockets)
        if (s !== server && s.readyState === 1) s.send(ev.data);
    });
    const drop = () => this.sockets.delete(server);
    server.addEventListener("close", drop);
    server.addEventListener("error", drop);
    return new Response(null, { status: 101, webSocket: client });
  }
}
export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    if (url.pathname === "/health") return new Response("ok");
    if (url.pathname !== "/ws") return new Response("sino-relay: use /ws?id=<device>");
    const id = (url.searchParams.get("id") || "g52").slice(0, 64);
    return env.ROOM.get(env.ROOM.idFromName(id)).fetch(request);
  }
};
