package br.eni.cora;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

public class PulseView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float phase = 0f;
    private final Handler h = new Handler(Looper.getMainLooper());

    public PulseView(Context c, AttributeSet a) { super(c, a); init(); }
    public PulseView(Context c) { super(c); init(); }

    private void init() {
        paint.setColor(0xFF50FF8C);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f);
        paint.setStrokeCap(Paint.Cap.ROUND);
        h.postDelayed(new Runnable() {
            @Override public void run() {
                phase += 0.025f;
                if (phase > 1f) phase = 0f;
                invalidate();
                h.postDelayed(this, 40);
            }
        }, 40);
    }

    private void batida(Canvas c, float x0, float cy, float amp) {
        c.drawLine(x0, cy, x0 + 18, cy, paint);
        c.drawLine(x0 + 18, cy, x0 + 24, cy - amp * 0.75f, paint);
        c.drawLine(x0 + 24, cy - amp * 0.75f, x0 + 32, cy + amp, paint);
        c.drawLine(x0 + 32, cy + amp, x0 + 38, cy - amp * 0.35f, paint);
        c.drawLine(x0 + 38, cy - amp * 0.35f, x0 + 44, cy, paint);
        c.drawLine(x0 + 44, cy, x0 + 88, cy, paint);
    }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        float w = getWidth(), hh = getHeight();
        float cy = hh / 2f;
        float amp = hh * 0.32f;
        float bw = 88f;
        float x = -(phase * bw);
        while (x < w + bw) {
            batida(c, x, cy, amp);
            x += bw;
        }
    }
}
