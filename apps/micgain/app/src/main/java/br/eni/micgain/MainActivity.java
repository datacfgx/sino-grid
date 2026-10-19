package br.eni.micgain;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
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
    private Spinner spFonte;
    private SeekBar sbGanho;
    private TextView txtStatus, txtGanho;
    private Button btnRec;
    private volatile float ganho = 1.0f;
    private boolean gravando = false, cameraAberta = false;
    private String cameraId;
    private Uri videoUri;
    private ParcelFileDescriptor pfd;
    private final Handler ui = new Handler(Looper.getMainLooper());

    private static final String[] NOMES = {"UNPROCESSED (limpo p/ mic ext)", "MIC (padrao)", "CAMCORDER", "VOICE_RECOGNITION"};
    private static final int[] FONTES = {
            MediaRecorder.AudioSource.UNPROCESSED,
            MediaRecorder.AudioSource.MIC,
            MediaRecorder.AudioSource.CAMCORDER,
            MediaRecorder.AudioSource.VOICE_RECOGNITION};

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        tvPreview = findViewById(R.id.tvPreview);
        spFonte = findViewById(R.id.spFonte);
        sbGanho = findViewById(R.id.sbGanho);
        txtStatus = findViewById(R.id.txtStatus);
        txtGanho = findViewById(R.id.txtGanho);
        btnRec = findViewById(R.id.btnRec);
        tvPreview.setSurfaceTextureListener(this);
        spFonte.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, NOMES));
        sbGanho.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean u) {
                ganho = 1.0f + p / 10f;
                txtGanho.setText(String.format("Ganho do som: %.1fx (aplicado no arquivo final)", ganho));
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });
        btnRec.setOnClickListener(v -> { if (gravando) parar(); else iniciar(); });
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 1);
    }

    @Override public void onSurfaceTextureAvailable(SurfaceTexture st, int w, int h) { abrirCamera(); }
    @Override public void onSurfaceTextureSizeChanged(SurfaceTexture st, int w, int h) {}
    @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture st) { return false; }
    @Override public void onSurfaceTextureUpdated(SurfaceTexture st) {}

    private void abrirCamera() {
        if (cameraAberta) return;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) return;
        try {
            cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            for (String id : cameraManager.getCameraIdList()) {
                CameraCharacteristics c = cameraManager.getCameraCharacteristics(id);
                if (c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) { cameraId = id; break; }
            }
            if (cameraId == null) cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override public void onOpened(CameraDevice cd) { camera = cd; cameraAberta = true; txtStatus.setText("camera pronta"); }
                @Override public void onDisconnected(CameraDevice cd) { cd.close(); cameraAberta = false; }
                @Override public void onError(CameraDevice cd, int e) { cd.close(); cameraAberta = false; txtStatus.setText("erro camera " + e); }
            }, ui);
        } catch (Exception e) { txtStatus.setText("falha: " + e.getMessage()); }
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
            recorder.setAudioSource(FONTES[spFonte.getSelectedItemPosition()]);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setOutputFile(pfd.getFileDescriptor());
            recorder.setVideoEncodingBitRate(10_000_000);
            recorder.setVideoFrameRate(30);
            recorder.setVideoSize(1280, 720);
            recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioEncodingBitRate(192_000);
            recorder.setAudioSamplingRate(44100);
            recorder.prepare();

            Surface previewSurface = new Surface(tvPreview.getSurfaceTexture());
            Surface recSurface = recorder.getSurface();
            camera.createCaptureSession(Arrays.asList(previewSurface, recSurface), new CameraCaptureSession.StateCallback() {
                @Override public void onConfigured(CameraCaptureSession s) {
                    session = s;
                    try {
                        CaptureRequest.Builder req = camera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                        req.addTarget(previewSurface);
                        req.addTarget(recSurface);
                        session.setRepeatingRequest(req.build(), null, ui);
                        recorder.start();
                        gravando = true;
                        btnRec.setText("PARAR E SALVAR");
                        txtStatus.setText("gravando... ganho sera aplicado: " + String.format("%.1fx", ganho));
                    } catch (Exception e) { txtStatus.setText("erro: " + e.getMessage()); }
                }
                @Override public void onConfigureFailed(CameraCaptureSession s) { txtStatus.setText("falha na sessao"); }
            }, ui);
        } catch (Exception e) { txtStatus.setText("falha: " + e.getMessage()); }
    }

    private void parar() {
        try { recorder.stop(); } catch (Exception ignored) {}
        try { recorder.release(); } catch (Exception ignored) {}
        try { session.stopRepeating(); session.close(); } catch (Exception ignored) {}
        try { pfd.close(); } catch (Exception ignored) {}
        ContentValues feito = new ContentValues();
        feito.put(MediaStore.Video.Media.IS_PENDING, 0);
        getContentResolver().update(videoUri, feito, null, null);
        gravando = false;
        btnRec.setText("GRAVAR VIDEO");
        txtStatus.setText("salvo na Galeria: Filmes/MicCam/\n(ganho " + String.format("%.1fx", ganho) + " na v3)");
        toast("video salvo na Galeria");
    }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }

    @Override
    protected void onDestroy() {
        if (gravando) parar();
        try { if (camera != null) camera.close(); } catch (Exception ignored) {}
        super.onDestroy();
    }
}
