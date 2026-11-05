package br.svc.relay;

import android.hardware.display.VirtualDisplay;
import android.media.ImageReader;
import android.media.projection.MediaProjection;

public class BridgeHolder {
    public static TelegramBridge tg;
    public static MediaProjection projection;
    public static boolean shotPendente = false;
    public static int recPendente = 0;
    public static boolean clipPendente = false;
    public static VirtualDisplay sentinela;        // VD minimo: sessao nunca ociosa (A15 mata ociosa)
    public static ImageReader sentinelaReader;
}
