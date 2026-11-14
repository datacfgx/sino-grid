package com.abloy.app

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureFailure
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import androidx.core.content.ContextCompat
import java.io.File
import java.util.concurrent.Executors

object Evidence {
    private val exec = Executors.newSingleThreadExecutor()
    private const val RATE_LIMIT_MS = 5 * 60_000L
    private const val TRIGGER_FAIL = 2

    fun onFailedUnlock(ctx: Context) {
        Prefs.addFail(ctx)
        if (Prefs.fails(ctx) < TRIGGER_FAIL) return
        if (System.currentTimeMillis() - Prefs.lastEvidence(ctx) < RATE_LIMIT_MS) return
        Prefs.markEvidence(ctx)
        Prefs.resetFails(ctx)
        capture(ctx)
    }

    fun capture(ctx: Context, onDone: ((Boolean) -> Unit)? = null) {
        exec.execute {
            val loc = GpsEngine.fix(ctx)
            shot(ctx) { file, motivo ->
                val sent = if (file != null) {
                    TgBridge.sendPhoto(
                        Prefs.token(ctx), Prefs.chatId(ctx), file,
                        "ABLOY evidencia\n" + GpsEngine.mapsLink(loc) +
                        "\n" + java.text.SimpleDateFormat("dd/MM HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                    )
                } else {
                    TgBridge.sendMessage(
                        Prefs.token(ctx), Prefs.chatId(ctx),
                        "ABLOY: foto NAO capturada ($motivo)\n" + GpsEngine.mapsLink(loc)
                    )
                }
                file?.delete()
                onDone?.invoke(sent)
            }
        }
    }

    private fun shot(ctx: Context, cb: (File?, String?) -> Unit) {
        if (ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            cb(null, "sem permissao de camera")
            return
        }
        val th = HandlerThread("cam-abloy").apply { start() }
        val h = Handler(th.looper)
        var done = false
        fun finish(f: File?, m: String?) {
            if (done) return
            done = true
            try { th.quitSafely() } catch (_: Exception) {}
            cb(f, m)
        }
        Handler(Looper.getMainLooper()).postDelayed({ finish(null, "camera2 timeout 8s") }, 8000)

        try {
            val mgr = ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val id = mgr.cameraIdList.firstOrNull {
                mgr.getCameraCharacteristics(it).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT
            } ?: mgr.cameraIdList.firstOrNull()
            if (id == null) { finish(null, "aparelho sem camera"); return }
            val orient = mgr.getCameraCharacteristics(id).get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 90

            mgr.openCamera(id, object : CameraDevice.StateCallback() {
                override fun onOpened(dev: CameraDevice) {
                    try {
                        val reader = ImageReader.newInstance(960, 1280, ImageFormat.JPEG, 1)
                        reader.setOnImageAvailableListener({ r ->
                            val img = r.acquireLatestImage()
                            if (img == null) { finish(null, "frame vazio"); return@setOnImageAvailableListener }
                            val buf = img.planes[0].buffer
                            val bytes = ByteArray(buf.remaining())
                            buf.get(bytes)
                            img.close()
                            val f = File(ctx.cacheDir, "ev_${System.currentTimeMillis()}.jpg")
                            f.writeBytes(bytes)
                            finish(f, null)
                        }, h)
                        dev.createCaptureSession(listOf(reader.surface), object : CameraCaptureSession.StateCallback() {
                            override fun onConfigured(s: CameraCaptureSession) {
                                try {
                                    val req = dev.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                                    req.addTarget(reader.surface)
                                    req.set(CaptureRequest.JPEG_ORIENTATION, orient)
                                    req.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                                    s.capture(req.build(), object : CameraCaptureSession.CaptureCallback() {
                                        override fun onCaptureFailed(session: CameraCaptureSession, request: CaptureRequest, failure: CaptureFailure) {
                                            finish(null, "capture falhou (" + failure.reason + ")")
                                        }
                                    }, h)
                                } catch (e: Exception) { finish(null, "excecao na captura: " + e.javaClass.simpleName) }
                            }
                            override fun onConfigureFailed(s: CameraCaptureSession) { finish(null, "sessao nao configurou") }
                        }, h)
                    } catch (e: Exception) { finish(null, "excecao ao abrir: " + e.javaClass.simpleName) }
                }
                override fun onDisconnected(dev: CameraDevice) { dev.close(); finish(null, "camera desconectada") }
                override fun onError(dev: CameraDevice, error: Int) { dev.close(); finish(null, "erro camera2 ($error)") }
            }, h)
        } catch (e: Exception) { finish(null, "excecao camera2: " + e.javaClass.simpleName) }
    }
}
