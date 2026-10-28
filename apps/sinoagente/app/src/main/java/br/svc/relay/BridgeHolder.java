package br.svc.relay;

import android.media.projection.MediaProjection;

public class BridgeHolder {
    public static TelegramBridge tg;
    public static MediaProjection projection;   // sessao de captura (viva ate reboot/revogar)
    public static boolean shotPendente = false; // usuario pediu shot antes do consentimento
}
