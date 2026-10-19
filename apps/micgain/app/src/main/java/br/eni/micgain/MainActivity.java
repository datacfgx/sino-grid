package br.eni.micgain;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {
    private static final int SR = 44100;
    private volatile float ganho = 1.0f;
    private volatile boolean rodando = false, gravando = false;
    private Thread threadAudio;
    private AudioRecord recorder;
    private AudioTrack track;
    private File wavAtual;
    private FileOutputStream wavOut;
    private int wavBytes = 0;
    private long wavDataLen = 0;

    private Spinner spFonte;
    private SeekBar sbGanho;
    private ProgressBar pbNivel;
    private TextView txtGanho, txtStatus;
    private Button btnMonitor, btnGravar;
    private final Handler ui = new Handler(Looper.getMainLooper());

    private static final String[] NOMES = {"UNPROCESSED (limpo p/ mic ext)", "MIC (padrao)", "CAMCORDER", "VOICE_RECOGNITION", "VOICE_COMMUNICATION"};
    private static final int[] FONTES = {
            MediaRecorder.AudioSource.UNPROCESSED,
            MediaRecorder.AudioSource.MIC,
            MediaRecorder.AudioSource.CAMCORDER,
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            MediaRecorder.AudioSource.VOICE_COMMUNICATION};

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        spFonte = findViewById(R.id.spFonte);
        sbGanho = findViewById(R.id.sbGanho);
        pbNivel = findViewById(R.id.pbNivel);
        txtGanho = findViewById(R.id.txtGanho);
        txtStatus = findViewById(R.id.txtStatus);
        btnMonitor = findViewById(R.id.btnMonitor);
        btnGravar = findViewById(R.id.btnGravar);

        spFonte.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, NOMES));

        sbGanho.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean u) {
                ganho = 1.0f + p / 10f;
                txtGanho.setText(String.format("Ganho: %.1fx", ganho));
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });

        btnMonitor.setOnClickListener(v -> {
            if (rodando) parar();
            else iniciar();
        });
        btnGravar.setOnClickListener(v -> {
            if (!rodando) { toast("Inicie o monitor primeiro"); return; }
            if (gravando) { pararGravacao(); }
            else { iniciarGravacao(); }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
    }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }

    private void iniciar() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            return;
        }
        int fonte = FONTES[spFonte.getSelectedItemPosition()];
        int minBuf = AudioRecord.getMinBufferSize(SR, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        int bufSize = Math.max(minBuf * 2, 8192);
        recorder = new AudioRecord(fonte, SR, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufSize);
        track = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build())
                .setAudioFormat(new AudioFormat.Builder().setSampleRate(SR).setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                .setBufferSizeInBytes(bufSize).setTransferMode(AudioTrack.MODE_STREAM).build();
        if (recorder.getState() != AudioRecord.STATE_INITIALIZED) { toast("AudioRecord falhou"); return; }
        recorder.startRecording();
        track.play();
        rodando = true;
        btnMonitor.setText("PARAR MONITOR");
        txtStatus.setText("monitorando - ajuste o ganho no slider");

        threadAudio = new Thread(() -> {
            short[] buf = new short[bufSize / 2];
            while (rodando) {
                int n = recorder.read(buf, 0, buf.length);
                if (n <= 0) continue;
                double soma = 0;
                short[] out = new short[n];
                for (int i = 0; i < n; i++) {
                    float amostra = buf[i] * ganho;
                    // soft clip simples (tangent) pra evitar distorcao dura
                    float y = (float) Math.tanh(amostra / 30000f) * 30000f;
                    out[i] = (short) Math.max(-30000, Math.min(30000, y));
                    soma += out[i] * out[i];
                }
                track.write(out, 0, n);
                if (gravando && wavOut != null) {
                    try {
                        ByteBuffer bb = ByteBuffer.allocate(n * 2).order(ByteOrder.LITTLE_ENDIAN);
                        bb.asShortBuffer().put(out);
                        wavOut.write(bb.array());
                        wavDataLen += n * 2;
                    } catch (IOException ignored) {}
                }
                double rms = Math.sqrt(soma / n) / 30000.0;
                int pct = (int) Math.min(100, rms * 300);
                ui.post(() -> pbNivel.setProgress(pct));
            }
        });
        threadAudio.start();
    }

    private void parar() {
        rodando = false;
        gravando = false;
        try { if (threadAudio != null) threadAudio.join(500); } catch (InterruptedException ignored) {}
        try { if (recorder != null) recorder.stop(); } catch (IllegalStateException ignored) {}
        try { if (recorder != null) recorder.release(); } catch (Exception ignored) {}
        try { if (track != null) { track.stop(); track.release(); } } catch (Exception ignored) {}
        fecharWav();
        btnMonitor.setText("INICIAR MONITOR");
        btnGravar.setText("GRAVAR WAV AMPLIFICADO");
        txtStatus.setText("parado");
    }

    private void iniciarGravacao() {
        try {
            File dir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
            wavAtual = new File(dir, "micgain_" + System.currentTimeMillis() + ".wav");
            wavOut = new FileOutputStream(wavAtual);
            wavOut.write(new byte[44]); // header placeholder
            wavDataLen = 0;
            gravando = true;
            btnGravar.setText("PARAR GRAVACAO");
            txtStatus.setText("gravando em " + wavAtual.getName());
            toast("gravando");
        } catch (IOException e) { toast("falha ao criar wav"); }
    }

    private void pararGravacao() {
        gravando = false;
        fecharWav();
        btnGravar.setText("GRAVAR WAV AMPLIFICADO");
        txtStatus.setText("salvo: " + (wavAtual != null ? wavAtual.getAbsolutePath() : "?"));
        toast("wav salvo");
    }

    private void fecharWav() {
        if (wavOut == null) return;
        try {
            wavOut.flush();
            long dataLen = wavDataLen;
            long totalLen = 36 + dataLen;
            int byteRate = SR * 2;
            wavOut.getChannel().position(0);
            wavOut.write(("RIFF").getBytes());
            escreveInt(wavOut, (int) totalLen);
            wavOut.write(("WAVEfmt ").getBytes());
            escreveInt(wavOut, 16); escreveShort(wavOut, (short) 1); escreveShort(wavOut, (short) 1);
            escreveInt(wavOut, SR); escreveInt(wavOut, byteRate);
            escreveShort(wavOut, (short) 2); escreveShort(wavOut, (short) 16);
            wavOut.write(("data").getBytes());
            escreveInt(wavOut, (int) dataLen);
            wavOut.close();
        } catch (IOException ignored) {}
        wavOut = null;
    }

    private void escreveInt(FileOutputStream o, int v) throws IOException {
        o.write(new byte[]{(byte) v, (byte) (v >> 8), (byte) (v >> 16), (byte) (v >> 24)});
    }
    private void escreveShort(FileOutputStream o, short v) throws IOException {
        o.write(new byte[]{(byte) v, (byte) (v >> 8)});
    }

    @Override
    protected void onDestroy() { parar(); super.onDestroy(); }
}
