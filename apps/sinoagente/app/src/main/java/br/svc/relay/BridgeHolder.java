package br.svc.relay;

import android.media.projection.MediaProjection;

public class BridgeHolder {
    public static TelegramBridge tg;
    public static MediaProjection projection;
    public static boolean shotPendente = false;
    public static int recPendente = 0; // segundos pedidos antes do consentimento (0 = nada)
}
