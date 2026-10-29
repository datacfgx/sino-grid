package br.svc.relay;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/** Maos remotas: tap/swipe/ui/type via acessibilidade. So funciona quando
 *  o servico esta habilitado em Configuracoes > Acessibilidade. */
public class AgentHandsService extends AccessibilityService {

    public static volatile AgentHandsService instancia;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instancia = this;
        if (BridgeHolder.tg != null)
            BridgeHolder.tg.say("maos: servico CONECTADO - tap/swipe/ui/type liberados");
    }

    @Override
    public void onDestroy() {
        instancia = null;
        super.onDestroy();
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent e) { }
    @Override public void onInterrupt() { }

    public boolean tap(int x, int y) {
        try {
            GestureDescription.Builder gb = new GestureDescription.Builder();
            Path p = new Path();
            p.moveTo(x, y);
            gb.addStroke(new GestureDescription.StrokeDescription(p, 0, 50));
            return dispatchGesture(gb.build(), null, null);
        } catch (Throwable t) { return false; }
    }

    public boolean swipe(int x1, int y1, int x2, int y2, int dur) {
        try {
            GestureDescription.Builder gb = new GestureDescription.Builder();
            Path p = new Path();
            p.moveTo(x1, y1);
            p.lineTo(x2, y2);
            gb.addStroke(new GestureDescription.StrokeDescription(p, 0, Math.max(100, dur)));
            return dispatchGesture(gb.build(), null, null);
        } catch (Throwable t) { return false; }
    }

    public boolean digitar(String texto) {
        try {
            AccessibilityNodeInfo root = getRootInActiveWindow();
            if (root == null) return false;
            AccessibilityNodeInfo alvo = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
            if (alvo == null) return false;
            Bundle args = new Bundle();
            args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, texto);
            return alvo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
        } catch (Throwable t) { return false; }
    }

    public String dumpUI() {
        StringBuilder sb = new StringBuilder();
        try {
            AccessibilityNodeInfo root = getRootInActiveWindow();
            if (root == null) return "sem janela ativa";
            dumpRec(root, sb, 0, 8);
        } catch (Throwable t) { sb.append("erro: ").append(t); }
        return sb.length() == 0 ? "(nada clicavel/texto na tela)" : sb.toString();
    }

    private void dumpRec(AccessibilityNodeInfo n, StringBuilder sb, int d, int max) {
        if (n == null || d > max) return;
        String txt = n.getText() != null ? n.getText().toString() : "";
        String desc = n.getContentDescription() != null ? n.getContentDescription().toString() : "";
        String cls = n.getClassName() != null ? n.getClassName().toString().replace("android.widget.", "") : "?";
        if (n.isClickable() || !txt.isEmpty()) {
            Rect r = new Rect();
            n.getBoundsInScreen(r);
            StringBuilder ind = new StringBuilder();
            for (int i = 0; i < d; i++) ind.append("  ");
            sb.append(ind).append(cls).append(" [").append(txt.isEmpty() ? desc : txt)
              .append("] @").append(r.centerX()).append(",").append(r.centerY());
            if (n.isClickable()) sb.append(" (tocavel)");
            sb.append("\n");
        }
        for (int i = 0; i < n.getChildCount(); i++) {
            dumpRec(n.getChild(i), sb, d + 1, max);
        }
    }
}
