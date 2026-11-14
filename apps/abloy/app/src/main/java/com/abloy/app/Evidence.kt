package com.abloy.app

import android.content.Context
import android.location.Location
import android.os.Handler
import android.os.Looper
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import java.io.File
import java.util.concurrent.Executors

class SvcLifecycle : LifecycleOwner {
    private val reg = LifecycleRegistry(this).apply { currentState = Lifecycle.State.RESUMED }
    override val lifecycle: Lifecycle
        get() = reg
}

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
              exec.execute {
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
    }

    private fun shot(ctx: Context, cb: (File?, String?) -> Unit) {
        if (ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.CAMERA)
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            cb(null, "sem permissao de camera — conceda nas configuracoes do app")
            return
        }
        val future = ProcessCameraProvider.getInstance(ctx)
        val main = ContextCompat.getMainExecutor(ctx)
        future.addListener({
            try {
                val provider = future.get()
                val selector = if (provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA))
                    CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                val cap = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                val stub = ImageAnalysis.Builder().build().also {
                    it.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { img -> img.close() }
                }
                provider.unbindAll()
                provider.bindToLifecycle(SvcLifecycle(), selector, cap, stub)
                val file = File(ctx.cacheDir, "ev_${System.currentTimeMillis()}.jpg")
                cap.takePicture(ImageCapture.OutputFileOptions.Builder(file).build(), Executors.newSingleThreadExecutor(),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(o: ImageCapture.OutputFileResults) {
                            provider.unbindAll(); cb(file, null)
                        }
                        override fun onError(e: ImageCaptureException) {
                            provider.unbindAll(); cb(null, "capture erro " + e.imageCaptureError)
                        }
                    })
            } catch (e: Exception) { cb(null, "excecao: " + e.javaClass.simpleName) }
        }, main)
    }
}
