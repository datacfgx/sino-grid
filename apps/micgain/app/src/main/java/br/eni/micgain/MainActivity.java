package br.eni.micgain;

import android.Manifest;
import android.content.ContentValues;
import android.graphics.Color;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Size;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.Surface;
import android.view.TextureView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {
    private CameraManager cameraManager;
    private CameraDevice camera;
    private CameraCaptureSession session;
    private MediaRecorder recorder;
    private TextureView tvPreview;
    private int fonteSel = 4;
    private android.media.AudioManager audioManager;
    private boolean scoAtivo = false;

    private void ativarBluetooth() {
        try {
            if (audioManager == null) audioManager = (android.media.AudioManager) getSystemService(AUDIO_SERVICE);
            audioManager.setMode(android.media.AudioManager.MODE_IN_COMMUNICATION);
            audioManager.startBluetoothSco();
            audioManager.setBluetoothScoOn(true);
            scoAtivo = true;
        } catch (Exception ignored) {}
    }

    private void desativarBluetooth() {
        if (!scoAtivo) return;
        scoAtivo = false;
        try {
            audioManager.stopBluetoothSco();
            audioManager.setBluetoothScoOn(false);
            audioManager.setMode(android.media.AudioManager.MODE_NORMAL);
        } catch (Exception ignored) {}
    }

    private android.media.AudioDeviceInfo acharMicBt() {
        if (android.os.Build.VERSION.SDK_INT < 23) return null;
        try {
            if (audioManager == null) audioManager = (android.media.AudioManager) getSystemService(AUDIO_SERVICE);
            for (android.media.AudioDeviceInfo d : audioManager.getDevices(android.media.AudioManager.GET_DEVICES_INPUTS)) {
                int t = d.getType();
                if (t == android.media.AudioDeviceInfo.TYPE_BLUETOOTH_SCO
                        || (android.os.Build.VERSION.SDK_INT >= 31 && t == android.media.AudioDeviceInfo.TYPE_BLE_HEADSET)) {
                    return d;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }
    private android.widget.Button btnFonte;
    private MixerFaderView faderGanho;
    private TextView txtStatus, txtGanho;
    private Button btnRec;
    private volatile float ganho = 1.0f;
    private boolean gravando = false, cameraAberta = false;
    private int tentativasCamera = 0;
    private String cameraId;
    private String idCostas;
    private Uri videoUri;
    private ParcelFileDescriptor pfd;
    private final Handler ui = new Handler(Looper.getMainLooper());
    private long t0 = 0;
    private long previewOkAt = 0;
    private boolean ultimaGravProcessador = false;
    private int resW = 1920, resH = 1080;
    private int bitrate = 20_000_000;
    private boolean blinkOn = false;
    private final Runnable blinker = new Runnable() {
        @Override public void run() {
            if (!gravando) return;
            long seg = (System.currentTimeMillis() - t0) / 1000;
            txtStatus.setText(String.format("gravando... %02d:%02d | ganho: %.1fx (v3)", seg / 60, seg % 60, ganho));
            blinkOn = !blinkOn;
            btnRec.setTextColor(blinkOn ? Color.parseColor("#FF1744") : Color.parseColor("#550000"));
            ui.postDelayed(this, 500);
        }
    };

    private static final String[] NOMES = {"UNPROCESSED (limpo p/ mic ext)", "MIC (padrao)", "CAMCORDER", "VOICE_RECOGNITION", "PROCESSADOR (EQ + ganho no arquivo)", "BLUETOOTH (mic BT)"};
    private static final int[] FONTES = {
            MediaRecorder.AudioSource.UNPROCESSED,
            MediaRecorder.AudioSource.MIC,
            MediaRecorder.AudioSource.CAMCORDER,
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            -1,
            -2};

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        try {
        setContentView(R.layout.activity_main);
        tvPreview = findViewById(R.id.tvPreview);
        tvPreview.setSurfaceTextureListener(this);
        if (tvPreview.isAvailable()) {   // surface ja pronta antes do listener (corrida)
            surfacePronta = true;
            abrirCamera();
            ui.postDelayed(this::iniciarPreviewOcioso, 500);
        }
        btnFonte = findViewById(R.id.btnFonte);
        btnFonte.setText(NOMES[fonteSel]);
        btnFonte.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Fonte do microfone")
                    .setSingleChoiceItems(NOMES, fonteSel, (d, i) -> {
                        fonteSel = i;
                        btnFonte.setText(NOMES[i]);
                        if (i == 5 && android.os.Build.VERSION.SDK_INT >= 31) {
                            try { requestPermissions(new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 77); } catch (Exception ignored) {}
                        }
                        d.dismiss();
                    })
                    .show();
        });
        txtStatus = findViewById(R.id.txtStatus);
        txtGanho = findViewById(R.id.txtGanho);
        faderGanho = findViewById(R.id.faderGanho);
        faderGanho.setOnValorListener(v -> {
            ganho = 1.0f + v / 10f;
            txtGanho.setText(String.format("%.1fx", ganho));
        });
        btnRec = findViewById(R.id.btnRec);
        btnRec.setTextColor(Color.WHITE);
        
        android.widget.Button btnRes = findViewById(R.id.btnRes);
        btnRes.setOnClickListener(v -> {
            String[] opcoes = {"HD 720p (leve, ocupa menos)", "Full HD 1080p (qualidade máxima)"};
            int atual = (resW == 1920) ? 1 : 0;
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Resolução do vídeo")
                    .setSingleChoiceItems(opcoes, atual, (d, i) -> {
                        if (i == 1) { resW = 1920; resH = 1080; bitrate = 20_000_000; }
                        else { resW = 1280; resH = 720; bitrate = 10_000_000; }
                        btnRes.setText((i == 1 ? "\u2699\uFE0F Resolução: Full HD 1080p" : "\u2699\uFE0F Resolução: HD 720p"));
                        d.dismiss();
                    })
                    .show();
        });
        btnRec.setOnClickListener(v -> { if (gravando) parar(); else iniciar(); });
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 1);
        } catch (Throwable t) {
            StringBuilder sb = new StringBuilder("ERRO NO APP:\n\n").append(t.getClass().getSimpleName()).append(": ").append(t.getMessage()).append("\n\n");
            for (StackTraceElement e : t.getStackTrace()) sb.append(e.toString()).append("\n");
            new androidx.appcompat.app.AlertDialog.Builder(this).setTitle("MicCam - erro de inicializacao").setMessage(sb.toString()).setPositiveButton("OK", null).show();
        }
    }
    private boolean surfacePronta = false;
    @Override public void onSurfaceTextureAvailable(SurfaceTexture st, int w, int h) {
        surfacePronta = true;
        abrirCamera();
        ui.postDelayed(() -> { if (cameraAberta && !gravando) iniciarPreviewOcioso(); }, 400);
    }
    @Override public void onSurfaceTextureSizeChanged(SurfaceTexture st, int w, int h) {}

        @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture st) { return false; }
    @Override public void onSurfaceTextureUpdated(SurfaceTexture st) {}

    private void abrirCamera() {
        if (cameraAberta) return;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            txtStatus.setText("permissao da camera negada");
            return;
        }
        try {
            cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            for (String id : cameraManager.getCameraIdList()) {
                CameraCharacteristics c = cameraManager.getCameraCharacteristics(id);
                Integer f = c.get(CameraCharacteristics.LENS_FACING);
                if (f != null && f == CameraCharacteristics.LENS_FACING_BACK) idCostas = id;
            }
            cameraId = idCostas != null ? idCostas : cameraManager.getCameraIdList()[0];
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override public void onOpened(CameraDevice cd) {
            camera = cd; cameraAberta = true; tentativasCamera = 0;
            txtStatus.setText("camera pronta");
            previewOkAt = 0;
            iniciarPreviewOcioso();
            ui.postDelayed(MainActivity.this::iniciarPreviewOcioso, 800);
            ui.postDelayed(MainActivity.this::iniciarPreviewOcioso, 2500);
        }
                @Override public void onDisconnected(CameraDevice cd) { cd.close(); cameraAberta = false; }
                @Override public void onError(CameraDevice cd, int e) {
                    try { cd.close(); } catch (Exception ignored) {}
                    camera = null; cameraAberta = false;
                    if (tentativasCamera < 4) { tentativasCamera++; txtStatus.setText("erro camera " + e + " — tentando de novo"); ui.postDelayed(MainActivity.this::abrirCamera, 800); }
                    else txtStatus.setText("erro camera " + e);
                }
            }, ui);
            tentativasCamera++;
            ui.postDelayed(MainActivity.this::vigiaCamera, 2600);
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder("falha: ").append(e).append("\n");
            for (StackTraceElement s : e.getStackTrace()) sb.append(s.toString()).append("\n");
            txtStatus.setText(sb.toString());
        }
    }

    private void vigiaCamera() {
        if (cameraAberta || gravando) return;
        if (tentativasCamera >= 4) { txtStatus.setText("camera nao respondeu — feche e abra o app"); return; }
        tentativasCamera++;
        try { if (camera != null) { camera.close(); camera = null; } } catch (Exception ignored) {}
        cameraAberta = false;
        txtStatus.setText("reativando camera... (tentativa " + tentativasCamera + ")");
        ui.postDelayed(this::abrirCamera, 500L * tentativasCamera);
    }

    private void iniciarPreviewOcioso() {
        if (camera == null || !surfacePronta) return;
        if (System.currentTimeMillis() - previewOkAt < 4000) return; // preview saudavel, nao reabre
        try {
            Surface s = new Surface(tvPreview.getSurfaceTexture());
            camera.createCaptureSession(java.util.Arrays.asList(s), new CameraCaptureSession.StateCallback() {
                @Override public void onConfigured(CameraCaptureSession sess) {
                    session = sess;
                    previewOkAt = System.currentTimeMillis();
                    try {
                        CaptureRequest.Builder r = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                        r.addTarget(s);
                        session.setRepeatingRequest(r.build(), null, ui);
                    } catch (Exception ignored) {}
                }
                @Override public void onConfigureFailed(CameraCaptureSession sess) {
                    ui.postDelayed(() -> { if (!gravando && cameraAberta) iniciarPreviewOcioso(); }, 600);
                }
            }, ui);
        } catch (Exception e) {
            ui.postDelayed(() -> { if (!gravando && cameraAberta) iniciarPreviewOcioso(); }, 600);
        }
    }

    private void iniciar() {
        if (camera == null) { abrirCamera(); toast("aguarde a camera"); return; }
        try {
            String nome = "miccam_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".mp4";
            ContentValues cv = new ContentValues();
            cv.put(MediaStore.Video.Media.DISPLAY_NAME, nome);
            cv.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
            cv.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/MicCam");
            cv.put(MediaStore.Video.Media.IS_PENDING, 1);
            videoUri = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, cv);
            pfd = getContentResolver().openFileDescriptor(videoUri, "w");
            recorder = new MediaRecorder();
            CameraCharacteristics cc = cameraManager.getCameraCharacteristics(cameraId);
            int sensorOrientation = cc.get(CameraCharacteristics.SENSOR_ORIENTATION);
            int rot = getWindowManager().getDefaultDisplay().getRotation();
            int graus = 0;
            switch (rot) {
                case Surface.ROTATION_90: graus = 90; break;
                case Surface.ROTATION_180: graus = 180; break;
                case Surface.ROTATION_270: graus = 270; break;
            }
            recorder.setOrientationHint((sensorOrientation - graus + 360) % 360);
            recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            int fonteEscolhida = FONTES[fonteSel];
            final boolean processador = fonteEscolhida == -1;
            ultimaGravProcessador = processador;
            final boolean bluetooth = fonteEscolhida == -2;
            if (bluetooth) {
                ativarBluetooth();
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                if (android.os.Build.VERSION.SDK_INT >= 28) {
                    try {
                        android.media.AudioDeviceInfo bt = acharMicBt();
                        if (bt != null) recorder.setPreferredDevice(bt);
                    } catch (Exception ignored) {}
                }
            } else if (processador) {
                recorder.setAudioSource(MediaRecorder.AudioSource.UNPROCESSED);
            } else {
                recorder.setAudioSource(fonteEscolhida);
            }
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setOutputFile(pfd.getFileDescriptor());
            recorder.setVideoEncodingBitRate(bitrate);
            recorder.setVideoFrameRate(30);
            recorder.setVideoSize(resW, resH);
            recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioEncodingBitRate(192_000);
            recorder.setAudioSamplingRate(44100);
            recorder.prepare();

            try { if (session != null) { session.stopRepeating(); session.close(); } } catch (Exception ignored) {}
            Surface previewSurface = new Surface(tvPreview.getSurfaceTexture());
            Surface recSurface = recorder.getSurface();
            camera.createCaptureSession(Arrays.asList(previewSurface, recSurface), new CameraCaptureSession.StateCallback() {
                @Override public void onConfigured(CameraCaptureSession s) {
                    session = s;
                    try {
                        CaptureRequest.Builder req = camera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                        req.addTarget(previewSurface);
                        req.addTarget(recSurface);
                        try {
                            int[] ois = cc.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION);
                            if (ois != null) for (int m : ois) if (m == CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON)
                                { req.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON); break; }
                        } catch (Exception ignored) {}
                        try { req.set(CaptureRequest.EDGE_MODE, CameraMetadata.EDGE_MODE_HIGH_QUALITY); } catch (Exception ignored) {}
                        try { req.set(CaptureRequest.NOISE_REDUCTION_MODE, CameraMetadata.NOISE_REDUCTION_MODE_HIGH_QUALITY); } catch (Exception ignored) {}
                        try { req.set(CaptureRequest.HOT_PIXEL_MODE, CameraMetadata.HOT_PIXEL_MODE_HIGH_QUALITY); } catch (Exception ignored) {}
                        try { req.set(CaptureRequest.TONEMAP_MODE, CameraMetadata.TONEMAP_MODE_HIGH_QUALITY); } catch (Exception ignored) {}
                        try {
                            int[] eis = cc.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES); // mantem
                            if (eis != null) for (int m : eis) if (m == CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_ON)
                                { req.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_ON); break; }
                        } catch (Exception ignored) {}
                        session.setRepeatingRequest(req.build(), null, ui);
                        if (processador) iniciarPcm(MediaRecorder.AudioSource.UNPROCESSED);
                        recorder.start();
                        gravando = true;
                        t0 = System.currentTimeMillis();
                        btnRec.setText("GRAVANDO - toque para parar");
                        btnRec.setTextColor(Color.WHITE);
                        ui.post(blinker);
                    } catch (Exception e) { txtStatus.setText("erro: " + e.getMessage()); }
                }
                @Override public void onConfigureFailed(CameraCaptureSession s) { txtStatus.setText("falha na sessao"); }
            }, ui);
        } catch (Exception e) { desativarBluetooth(); txtStatus.setText("falha: " + e.getMessage()); }
    }

    private void parar() {
        gravando = false;
        ui.removeCallbacks(blinker);
        btnRec.setText("SALVO!");
        btnRec.setTextColor(Color.WHITE);
        txtStatus.setText("finalizando...");
        new Thread(() -> {
            try { recorder.stop(); } catch (Exception ignored) {}
            try { recorder.release(); } catch (Exception ignored) {}
            try { session.stopRepeating(); session.close(); } catch (Exception ignored) {}
            try { pfd.close(); } catch (Exception ignored) {}
            desativarBluetooth();
            previewOkAt = 0;
            iniciarPreviewOcioso();
            ui.postDelayed(this::iniciarPreviewOcioso, 1500);
            ui.postDelayed(this::iniciarPreviewOcioso, 4000);
            if (ultimaGravProcessador) {
                pararPcm();
                runOnUiThread(() -> txtStatus.setText("processando EQ + ganho no audio..."));
                muxAudioProcessado();
            }
            ContentValues feito = new ContentValues();
            feito.put(MediaStore.Video.Media.IS_PENDING, 0);
            getContentResolver().update(videoUri, feito, null, null);
            long seg = (System.currentTimeMillis() - t0) / 1000;
            String extra = ultimaGravProcessador ? " | audio: PROCESSADO (EQ+ganho)" : " | audio limpo (ganho na v3 com processador)";
            runOnUiThread(() -> {
                txtStatus.setText(String.format("salvo na Galeria: Filmes/MicCam/ (%02d:%02d)%s", seg / 60, seg % 60, extra));
                toast("video salvo na Galeria");
                ui.postDelayed(() -> {
                    btnRec.setText("GRAVAR VIDEO");
                    btnRec.setTextColor(Color.WHITE);
        
        android.widget.Button btnRes = findViewById(R.id.btnRes);
        btnRes.setOnClickListener(v -> {
            String[] opcoes = {"HD 720p (leve, ocupa menos)", "Full HD 1080p (qualidade máxima)"};
            int atual = (resW == 1920) ? 1 : 0;
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Resolução do vídeo")
                    .setSingleChoiceItems(opcoes, atual, (d, i) -> {
                        if (i == 1) { resW = 1920; resH = 1080; bitrate = 20_000_000; }
                        else { resW = 1280; resH = 720; bitrate = 10_000_000; }
                        btnRes.setText((i == 1 ? "\u2699\uFE0F Resolução: Full HD 1080p" : "\u2699\uFE0F Resolução: HD 720p"));
                        d.dismiss();
                    })
                    .show();
        });
                }, 2500);
            });
        }).start();
    }


    // ===================== PROCESSADOR v3: captura PCM + EQ 3 bandas + mux =====================
    private android.media.AudioRecord pcmRec;
    private Thread pcmThread;
    private volatile boolean capturandoPcm = false;
    private final java.util.ArrayList<Short> pcmProcessado = new java.util.ArrayList<>();
    private double b0l, b1l, b2l, a1l, a2l, x1l, x2l, y1l, y2l;
    private double b0m, b1m, b2m, a1m, a2m, x1m, x2m, y1m, y2m;
    private double b0h, b1h, b2h, a1h, a2h, x1h, x2h, y1h, y2h;

    private void initEq() {
        double fs = 44100.0;
        // graves: low-shelf 120Hz +6dB
        double A = Math.pow(10, 6.0 / 40.0), w0 = 2 * Math.PI * 120 / fs, S = 1.0;
        double alfa = Math.sin(w0) / 2 * Math.sqrt((A + 1 / A) * (1 / S - 1) + 2);
        double cw = Math.cos(w0), sq = 2 * Math.sqrt(A) * alfa;
        double denl = (A + 1) + (A - 1) * cw + sq;
        b0l = A * ((A + 1) - (A - 1) * cw + sq) / denl;
        b1l = 2 * A * ((A - 1) - (A + 1) * cw) / denl;
        b2l = A * ((A + 1) - (A - 1) * cw - sq) / denl;
        a1l = -2 * ((A - 1) + (A + 1) * cw) / denl;
        a2l = ((A + 1) + (A - 1) * cw - sq) / denl;
        // medios: peaking 1kHz +3dB Q=0.8
        A = Math.pow(10, 3.0 / 40.0); w0 = 2 * Math.PI * 1000 / fs; double Q = 0.8;
        alfa = Math.sin(w0) / (2 * Q);
        double den = 1 + alfa / A;
        b0m = (1 + alfa * A) / den; b1m = (-2 * Math.cos(w0)) / den; b2m = (1 - alfa * A) / den;
        a1m = (-2 * Math.cos(w0)) / den; a2m = (1 - alfa / A) / den;
        // agudos: high-shelf 6kHz +4dB
        A = Math.pow(10, 4.0 / 40.0); w0 = 2 * Math.PI * 6000 / fs; S = 1.0;
        alfa = Math.sin(w0) / 2 * Math.sqrt((A + 1 / A) * (1 / S - 1) + 2);
        cw = Math.cos(w0); sq = 2 * Math.sqrt(A) * alfa;
        double d0 = (A + 1) + (A - 1) * cw + sq;
        b0h = A * ((A + 1) + (A - 1) * cw + sq) / d0; b1h = -2 * A * ((A - 1) + (A + 1) * cw) / d0;
        b2h = A * ((A + 1) + (A - 1) * cw - sq) / d0;
        a1h = -((A - 1) + (A + 1) * cw - sq) / d0; a2h = -((A + 1) - (A - 1) * cw + sq) / d0;
        x1l = x2l = y1l = y2l = x1m = x2m = y1m = y2m = x1h = x2h = y1h = y2h = 0;
    }
    private short eq3(short x) {
        double v = x;
        double y = b0l * v + b1l * x1l + b2l * x2l - a1l * y1l - a2l * y2l;
        x2l = x1l; x1l = v; y2l = y1l; y1l = y;
        v = y;
        y = b0m * v + b1m * x1m + b2m * x2m - a1m * y1m - a2m * y2m;
        x2m = x1m; x1m = v; y2m = y1m; y1m = y;
        v = y;
        y = b0h * v + b1h * x1h + b2h * x2h + a1h * y1h + a2h * y2h;
        x2h = x1h; x1h = v; y2h = y1h; y1h = y;
        double g = y * ganho;
        g = Math.tanh(g / 30000.0) * 30000.0;
        return (short) Math.max(-32000, Math.min(32000, g));
    }

    private void iniciarPcm(int fonte) {
        int minBuf = android.media.AudioRecord.getMinBufferSize(44100, android.media.AudioFormat.CHANNEL_IN_MONO, android.media.AudioFormat.ENCODING_PCM_16BIT);
        pcmRec = new android.media.AudioRecord(fonte, 44100, android.media.AudioFormat.CHANNEL_IN_MONO, android.media.AudioFormat.ENCODING_PCM_16BIT, Math.max(minBuf * 2, 8192));
        if (pcmRec.getState() != android.media.AudioRecord.STATE_INITIALIZED) return;
        pcmProcessado.clear();
        initEq();
        pcmRec.startRecording();
        capturandoPcm = true;
        pcmThread = new Thread(() -> {
            short[] buf = new short[2048];
            while (capturandoPcm) {
                int n = pcmRec.read(buf, 0, buf.length);
                if (n <= 0) continue;
                for (int i = 0; i < n; i++) pcmProcessado.add(eq3(buf[i]));
            }
        });
        pcmThread.start();
    }

    private void pararPcm() {
        capturandoPcm = false;
        try { if (pcmThread != null) pcmThread.join(400); } catch (InterruptedException ignored) {}
        try { pcmRec.stop(); pcmRec.release(); } catch (Exception ignored) {}
    }

    private void muxAudioProcessado() {
        if (pcmProcessado.size() < 4410) return;
        File tmp = null;
        try {
            tmp = File.createTempFile("miccam_mux", ".mp4", getCacheDir());
            android.media.MediaExtractor ex = new android.media.MediaExtractor();
            ex.setDataSource(this, videoUri, null);
            int vi = -1;
            for (int i = 0; i < ex.getTrackCount(); i++) {
                if (ex.getTrackFormat(i).getString(android.media.MediaFormat.KEY_MIME).startsWith("video/")) { vi = i; break; }
            }
            if (vi < 0) return;
            ex.selectTrack(vi);
            android.media.MediaMuxer muxer = new android.media.MediaMuxer(tmp.getAbsolutePath(), android.media.MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            android.media.MediaFormat vfmt = ex.getTrackFormat(vi);
            int vidx = muxer.addTrack(vfmt);
            android.media.MediaFormat afmt = android.media.MediaFormat.createAudioFormat("audio/mp4a-latm", 44100, 1);
            afmt.setInteger(android.media.MediaFormat.KEY_AAC_PROFILE, android.media.MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            afmt.setInteger(android.media.MediaFormat.KEY_BIT_RATE, 192000);
            int aidx = muxer.addTrack(afmt);
            muxer.start();
            java.nio.ByteBuffer vbuf = java.nio.ByteBuffer.allocate(1024 * 1024);
            android.media.MediaCodec.BufferInfo bi = new android.media.MediaCodec.BufferInfo();
            while (true) {
                bi.offset = 0; bi.size = ex.readSampleData(vbuf, 0);
                if (bi.size < 0) break;
                bi.presentationTimeUs = ex.getSampleTime(); bi.flags = ex.getSampleFlags();
                muxer.writeSampleData(vidx, vbuf, bi);
                ex.advance();
            }
            ex.release();
            android.media.MediaCodec enc = android.media.MediaCodec.createEncoderByType("audio/mp4a-latm");
            enc.configure(afmt, null, null, android.media.MediaCodec.CONFIGURE_FLAG_ENCODE);
            enc.start();
            java.nio.ByteBuffer inBuf = java.nio.ByteBuffer.allocateDirect(2048 * 2);
            java.nio.ByteBuffer[] outArr = enc.getOutputBuffers();
            int total = pcmProcessado.size(), pos = 0;
            long pts = 0;
            boolean inputDone = false, outputDone = false;
            while (!outputDone) {
                if (!inputDone) {
                    int idx = enc.dequeueInputBuffer(10000);
                    if (idx >= 0) {
                        inBuf.clear();
                        int chunk = Math.min(1024, total - pos);
                        for (int i = 0; i < chunk; i++) inBuf.putShort(pcmProcessado.get(pos + i));
                        pos += chunk;
                        enc.queueInputBuffer(idx, 0, chunk * 2, pts, pos >= total ? android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);
                        pts += (long) chunk * 1000000 / 44100;
                        if (pos >= total) inputDone = true;
                    }
                }
                int oi = enc.dequeueOutputBuffer(bi, 10000);
                if (oi == android.media.MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) outArr = enc.getOutputBuffers();
                else if (oi >= 0) {
                    if (bi.size > 0 && (bi.flags & android.media.MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                        muxer.writeSampleData(aidx, outArr[oi], bi);
                    }
                    enc.releaseOutputBuffer(oi, false);
                    if ((bi.flags & android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) outputDone = true;
                }
            }
            enc.stop(); enc.release();
            muxer.stop(); muxer.release();
            java.io.InputStream is = new java.io.FileInputStream(tmp);
            java.io.OutputStream os = getContentResolver().openOutputStream(videoUri, "wt");
            byte[] b = new byte[65536]; int n;
            while ((n = is.read(b)) > 0) os.write(b, 0, n);
            os.close(); is.close();
            tmp.delete();
        } catch (Exception e) {
            runOnUiThread(() -> txtStatus.setText("processador falhou (audio original mantido): " + e.getMessage()));
        }
    }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            abrirCamera();
            ui.postDelayed(() -> { if (cameraAberta && !gravando) iniciarPreviewOcioso(); }, 600);
        }
    }

    @Override
    protected void onDestroy() {
        if (gravando) parar();
        try { if (camera != null) camera.close(); } catch (Exception ignored) {}
        super.onDestroy();
    }
}
