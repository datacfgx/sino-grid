package com.abloy.app

import android.util.Log
import java.io.DataOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object TgBridge {
    private const val TAG = "AbloyTG"

    fun sendMessage(token: String, chat: String, text: String): Boolean {
        return try {
            val body = "chat_id=" + URLEncoder.encode(chat, "UTF-8") +
                       "&text=" + URLEncoder.encode(text, "UTF-8")
            val c = URL("https://api.telegram.org/bot$token/sendMessage").openConnection() as HttpURLConnection
            c.requestMethod = "POST"
            c.doOutput = true
            c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            DataOutputStream(c.outputStream).use { it.writeBytes(body) }
            val ok = c.responseCode == 200
            c.disconnect()
            ok
        } catch (e: Exception) { Log.w(TAG, "msg: ${e.message}"); false }
    }

    fun sendPhoto(token: String, chat: String, file: File, caption: String): Boolean {
        var erro = ""
        val ok = try {
            val boundary = "----Abloy" + System.currentTimeMillis()
            val body = java.io.ByteArrayOutputStream()
            java.io.DataOutputStream(body).use { out ->
                fun field(name: String, value: String) {
                    out.writeBytes("--$boundary\r\nContent-Disposition: form-data; name=\"$name\"\r\n\r\n$value\r\n")
                }
                field("chat_id", chat)
                field("caption", caption)
                out.writeBytes("--$boundary\r\nContent-Disposition: form-data; name=\"photo\"; filename=\"${file.name}\"\r\nContent-Type: image/jpeg\r\n\r\n")
                out.write(file.readBytes())
                out.writeBytes("\r\n--$boundary--\r\n")
            }
            val bytes = body.toByteArray()
            val c = URL("https://api.telegram.org/bot$token/sendPhoto").openConnection() as HttpURLConnection
            c.requestMethod = "POST"
            c.doOutput = true
            c.setFixedLengthStreamingMode(bytes.size)
            c.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            c.outputStream.use { it.write(bytes) }
            val code = c.responseCode
            if (code != 200) {
                erro = c.errorStream?.bufferedReader()?.readText()?.take(200) ?: ("HTTP " + code)
            }
            c.disconnect()
            code == 200
        } catch (e: Exception) { erro = e.javaClass.simpleName + ": " + (e.message ?: ""); false }
        if (!ok) lastPhotoError = erro
        return ok
    }

    var lastPhotoError: String = ""

    fun getUpdates(token: String, offset: Long, timeout: Int = 25): String? {
        return try {
            val c = URL("https://api.telegram.org/bot$token/getUpdates?offset=$offset&timeout=$timeout").openConnection() as HttpURLConnection
            c.connectTimeout = 30_000
            c.readTimeout = (timeout + 10) * 1000
            val s = c.inputStream.bufferedReader().readText()
            c.disconnect()
            s
        } catch (e: Exception) { Log.w(TAG, "poll: ${e.message}"); null }
    }
}
