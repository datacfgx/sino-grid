package br.eni.cora;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.widget.*;

public class MainActivity extends Activity {
    TextView status, battV, memV, dataV, aoVivo;
    View barB, barM, barD;
    TextView chipAdb;
    EditText host, devid;
    Handler h = new Handler();
    int dp(float v) { return (int) (v * getResources().getDisplayMetrics().density + 0.5f); }
    GradientDrawable r(int color, float rad) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(rad));
        return g;
    }

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        ScrollView sv = new ScrollView(this);
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.VERTICAL);
        r.setPadding(dp(20), dp(20), dp(20), dp(20));
        r.setBackgroundColor(0xFF08080C);

        // header
        LinearLayout hdr = new LinearLayout(this);
        hdr.setGravity(Gravity.CENTER_VERTICAL);
        TextView titulo = new TextView(this);
        titulo.setText("CORA 💓");
        titulo.setTextSize(24f);
        titulo.setTextColor(0xFFF2F3F7);
        titulo.setTypeface(null, android.graphics.Typeface.BOLD);
        aoVivo = new TextView(this);
        aoVivo.setText("  ● ao vivo");
        aoVivo.setTextSize(11f);
        aoVivo.setTextColor(0xFF50FF8C);
        aoVivo.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(0, -2, 1f);
        hdr.addView(titulo, sp);
        hdr.addView(aoVivo);
        r.addView(hdr);

        // card coração
        LinearLayout card = card();
        TextView ct = new TextView(this);
        ct.setText("Coração batendo");
        ct.setTextSize(17f);
        ct.setTextColor(0xFFF2F3F7);
        ct.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(ct);
        PulseView pv = new PulseView(this);
        LinearLayout.LayoutParams pvlp = new LinearLayout.LayoutParams(-1, dp(56));
        pvlp.topMargin = dp(8);
        card.addView(pv, pvlp);
        TextView sub = new TextView(this);
        sub.setText("sala " + Prefs.id(this) + " · túnel reverso ativo");
        sub.setTextSize(11f);
        sub.setTextColor(0xFF9AA0B4);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(-1, -2);
        slp.topMargin = dp(6);
        card.addView(sub, slp);
        r.addView(card);

        // card vitais
        LinearLayout vcard = card();
        TextView vt = new TextView(this);
        vt.setText("VITAIS DO APARELHO");
        vt.setTextSize(10f);
        vt.setTextColor(0xFF9AA0B4);
        vcard.addView(vt);
        LinearLayout.LayoutParams m8 = new LinearLayout.LayoutParams(-1, -2);
        m8.topMargin = dp(10);
        battV = linhaVital(vcard, "Bateria");
        barB = barra(vcard, 0xFF50FF8C);
        memV = linhaVital(vcard, "Memória livre");
        barM = barra(vcard, 0xFFFFB224);
        dataV = linhaVital(vcard, "Armazenamento livre");
        barD = barra(vcard, 0xFF50FF8C);
        r.addView(vcard);

        // chips
        LinearLayout chips = new LinearLayout(this);
        chips.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(-1, -2);
        clp.topMargin = dp(10);
        chipAdb = chip("depuração: ...", 0xFFFFB224);
        chips.addView(chipAdb);
        TextView boot = chip("inicia no boot ✓", 0xFF50FF8C);
        LinearLayout.LayoutParams bl = new LinearLayout.LayoutParams(-2, -2);
        bl.leftMargin = dp(8);
        chips.addView(boot, bl);
        r.addView(chips, clp);

        // botões
        LinearLayout.LayoutParams bg = new LinearLayout.LayoutParams(-1, dp(46));
        bg.topMargin = dp(10);
        Button diag = new Button(this);
        diag.setText("🩺 Diagnóstico remoto");
        diag.setTextColor(0xFF08080C);
        diag.setTypeface(null, android.graphics.Typeface.BOLD);
        diag.setBackground(r(0xFFF2F3F7, 10));
        diag.setOnClickListener(v -> {
            SinoService.wsSend("{\"cmd\":\"report\"}");
            SinoService.status = "diagnóstico solicitado à clínica";
        });
        r.addView(diag, bg);

        LinearLayout.LayoutParams bg2 = new LinearLayout.LayoutParams(-1, dp(44));
        bg2.topMargin = dp(8);
        Button sos = new Button(this);
        sos.setText("🆘 Preciso de ajuda");
        sos.setTextColor(0xFFFF5252);
        sos.setTypeface(null, android.graphics.Typeface.BOLD);
        sos.setBackground(r(0x00000000, 10));
        GradientDrawable sbg = r(0x00000000, 10);
        sbg.setStroke(dp(1.5f), 0xFFFF5252);
        sos.setBackground(sbg);
        sos.setOnClickListener(v -> {
            SinoService.wsSend("{\"type\":\"sos\",\"id\":\"" + Prefs.id(this) + "\"}");
            SinoService.status = "SOS enviado — a clínica foi avisada";
        });
        r.addView(sos, bg2);

        LinearLayout.LayoutParams bg3 = new LinearLayout.LayoutParams(-1, dp(42));
        bg3.topMargin = dp(8);
        Button par = new Button(this);
        par.setText("Parear novamente");
        par.setTextColor(0xFF9AA0B4);
        par.setBackground(r(0x00000000, 10));
        GradientDrawable pbg = r(0x00000000, 10);
        pbg.setStroke(dp(1), 0xFF5A6078);
        par.setBackground(pbg);
        par.setOnClickListener(v -> {
            SinoService.wsSend("{\"type\":\"quero_parear\",\"id\":\"" + Prefs.id(this) + "\"}");
            SinoService.status = "clínica avisada — abra a tela de pareamento";
        });
        r.addView(par, bg3);

        // status
        status = new TextView(this);
        status.setTextSize(13f);
        status.setTextColor(0xFFC8CCDC);
        LinearLayout.LayoutParams st = new LinearLayout.LayoutParams(-1, -2);
        st.topMargin = dp(14);
        r.addView(status, st);

        // config discreta
        TextView cfg = new TextView(this);
        cfg.setText("configuração");
        cfg.setTextSize(10f);
        cfg.setTextColor(0xFF5A6078);
        LinearLayout.LayoutParams cg = new LinearLayout.LayoutParams(-1, -2);
        cg.topMargin = dp(14);
        r.addView(cfg, cg);
        host = new EditText(this);
        host.setText(Prefs.host(this));
        host.setTextColor(0xFFC8CCDC);
        host.setTextSize(13f);
        host.setHintTextColor(0xFF5A6078);
        r.addView(host);
        devid = new EditText(this);
        devid.setText(Prefs.id(this));
        devid.setTextColor(0xFFC8CCDC);
        devid.setTextSize(13f);
        r.addView(devid);
        Button salvar = new Button(this);
        salvar.setText("SALVAR + INICIAR TÚNEL");
        salvar.setTextColor(0xFFC8CCDC);
        salvar.setBackground(r(0xFF1A1D28, 10));
        LinearLayout.LayoutParams svlp = new LinearLayout.LayoutParams(-1, dp(40));
        svlp.topMargin = dp(6);
        salvar.setOnClickListener(v -> {
            Prefs.set(this, host.getText().toString().trim(), devid.getText().toString().trim());
            startService(new Intent(this, SinoService.class));
        });
        r.addView(salvar, svlp);

        // footer
        LinearLayout foot = new LinearLayout(this);
        LinearLayout.LayoutParams flp = new LinearLayout.LayoutParams(-1, -2);
        flp.topMargin = dp(14);
        TextView f1 = new TextView(this);
        f1.setText("Cuidado ativo: Agente ENI 💓");
        f1.setTextSize(10f);
        f1.setTextColor(0xFF5A6078);
        TextView f2 = new TextView(this);
        f2.setText("br.eni.cora · v1.3");
        f2.setTextSize(10f);
        f2.setTextColor(0xFF5A6078);
        foot.addView(f1, new LinearLayout.LayoutParams(0, -2, 1f));
        foot.addView(f2);
        r.addView(foot, flp);

        sv.addView(r);
        setContentView(sv);
        startService(new Intent(this, SinoService.class));

        h.postDelayed(new Runnable() {
            @Override public void run() { atualiza(); h.postDelayed(this, 1500); }
        }, 500);
    }

    LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(14), dp(12), dp(14), dp(12));
        GradientDrawable bg = r(0xFF10121A, 12);
        bg.setStroke(dp(1), 0xFF2A2E42);
        c.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(12);
        return (LinearLayout) add(c, lp);
    }
    View add(View v, LinearLayout.LayoutParams lp) {
        // helper: retorna v adicionado ao último container — simplificação
        v.setLayoutParams(lp);
        return v;
    }
    TextView linhaVital(LinearLayout pai, String nome) {
        LinearLayout linha = new LinearLayout(this);
        TextView n = new TextView(this);
        n.setText(nome);
        n.setTextSize(12f);
        n.setTextColor(0xFF9AA0B4);
        TextView valor = new TextView(this);
        valor.setText("...");
        valor.setTextSize(12f);
        valor.setTextColor(0xFFF2F3F7);
        valor.setTypeface(null, android.graphics.Typeface.BOLD);
        linha.addView(n, new LinearLayout.LayoutParams(0, -2, 1f));
        linha.addView(valor);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(8);
        linha.setLayoutParams(lp);
        pai.addView(linha);
        return valor;
    }
    View barra(LinearLayout pai, int cor) {
        FrameLayout f = new FrameLayout(this);
        View bg = new View(this);
        bg.setBackground(r(0x22FFFFFF, 2));
        FrameLayout.LayoutParams bgp = new FrameLayout.LayoutParams(-1, dp(4));
        f.addView(bg, bgp);
        View fg = new View(this);
        fg.setBackground(r(cor, 2));
        FrameLayout.LayoutParams fgp = new FrameLayout.LayoutParams(0, dp(4));
        f.addView(fg, fgp);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(4));
        lp.topMargin = dp(4);
        f.setLayoutParams(lp);
        pai.addView(f);
        return fg;
    }
    TextView chip(String texto, int cor) {
        TextView c = new TextView(this);
        c.setText(texto);
        c.setTextSize(10f);
        c.setTextColor(cor);
        c.setTypeface(null, android.graphics.Typeface.BOLD);
        c.setPadding(dp(8), dp(4), dp(8), dp(4));
        GradientDrawable bg = r(0x15FFFFFF & (cor | 0xFF000000), 6);
        bg.setColor((cor & 0x00FFFFFF) | 0x26000000);
        bg.setStroke(dp(1), cor);
        c.setBackground(bg);
        return c;
    }

    void atualiza() {
        status.setText("status: " + SinoService.status);
        int batt = SinoService.lastBatt;
        if (batt >= 0) {
            battV.setText(batt + "%");
            setW(barB, batt);
        }
        long memMb = SinoService.lastMemKb / 1024;
        if (memMb >= 0) {
            memV.setText((memMb / 1024) + "," + ((memMb / 103) % 10) + " GB");
            int pct = (int) Math.min(100, memMb / 12);  // ~12GB total scale
            setW(barM, pct);
        }
        if (SinoService.lastDataMb >= 0) {
            dataV.setText(SinoService.lastDataMb / 1024 + " GB");
            setW(barD, (int) Math.min(100, SinoService.lastDataMb / 380));
        }
        boolean adbOn = SinoService.lastAdb == 1;
        chipAdb.setText(adbOn ? "depuração sem fio: ativa" : "depuração sem fio: desligada");
        aoVivo.setTextColor(SinoService.status.contains("conectado") || SinoService.status.contains("VALIDADO") ? 0xFF50FF8C : 0xFF5A6078);
    }
    void setW(View v, int pct) {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) v.getLayoutParams();
        lp.width = (int) (v.getParent() instanceof FrameLayout ? ((FrameLayout) v.getParent()).getWidth() * Math.max(2, Math.min(100, pct)) / 100f : 0);
        v.setLayoutParams(lp);
    }
}
