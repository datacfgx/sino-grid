package br.eni.micgain;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class MixerFaderView extends View {
    public interface OnValorListener { void onValor(float v); }
    private float valor = 0f; // 0..40  => 1.0x .. 5.0x
    private OnValorListener listener;
    private final Paint pRail = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pSafe = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pTick = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pTxt  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pCap  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pCapLine = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pGlow = new Paint(Paint.ANTI_ALIAS_FLAG);

    public MixerFaderView(Context c, AttributeSet a) { super(c, a); init(); }
    public MixerFaderView(Context c) { super(c); init(); }
    private void init() {
        pRail.setColor(0xFF282C3C); pSafe.setColor(0xFF3C5A46);
        pTick.setColor(0xFF5A6078); pTxt.setColor(0xFF8288A0);
        pCap.setColor(0xFF181A22); pCap.setStyle(Paint.Style.FILL);
        pCapLine.setColor(0xFF78FFAA); pCapLine.setStrokeWidth(6f);
        pGlow.setColor(0x552EC85A); pGlow.setStyle(Paint.Style.FILL);
    }

    private float pad(float w) { return Math.min(60f, w * 0.12f); }

    public void setOnValorListener(OnValorListener l) { listener = l; }
    public float getValor() { return valor; }
    public void setValor(float v) { valor = Math.max(0, Math.min(40, v)); invalidate(); }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        float w = getWidth(), h = getHeight();
        float p = pad(w);
        float y = h * 0.44f;
        float railH = Math.max(10f, Math.min(14f, h * 0.18f));
        float tickHalf = Math.min(20f, h * 0.22f);
        float labelOff = h * 0.36f;
        float txtSize = Math.max(20f, Math.min(26f, h * 0.28f));
        pTxt.setTextSize(txtSize);
        // trilho
        c.drawRoundRect(new RectF(p, y - railH/2, w - p, y + railH/2), 7, 7, pRail);
        // zona segura 0..30%
        float safeX = p + (w - 2*p) * 0.30f;
        c.drawRoundRect(new RectF(p, y - railH/2, safeX, y + railH/2), 7, 7, pSafe);
        // ticks + labels 1x..5x
        String[] labels = {"1x", "", "2x", "", "3x", "", "4x", "", "5x"};
        for (int i = 0; i < 9; i++) {
            float x = p + i * ((w - 2*p) / 8f);
            c.drawLine(x, y - tickHalf, x, y + tickHalf, pTick);
            if (!labels[i].isEmpty()) c.drawText(labels[i], x - txtSize * 0.7f, y + labelOff, pTxt);
        }
        // cap do fader
        float cx = p + (w - 2*p) * (valor / 40f);
        float capH = h * 0.60f, capW = capH * 1.32f;
        float lineHalf = capH * 0.36f;
        c.drawRoundRect(new RectF(cx - capW/2 - 8, y - capH/2 - 8, cx + capW/2 + 8, y + capH/2 + 8), 20, 20, pGlow);
        c.drawRoundRect(new RectF(cx - capW/2, y - capH/2, cx + capW/2, y + capH/2), 16, 16, pCap);
        c.drawLine(cx, y - lineHalf, cx, y + lineHalf, pCapLine);
    }

    @Override public boolean onTouchEvent(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_DOWN || e.getAction() == MotionEvent.ACTION_MOVE) {
            float p = pad(getWidth());
            float w = getWidth() - 2*p;
            valor = Math.max(0, Math.min(40, ((e.getX() - p) / w) * 40f));
            invalidate();
            if (listener != null) listener.onValor(valor);
            return true;
        }
        return true;
    }
}
