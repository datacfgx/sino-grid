package com.abloy.app

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AdminActivity : AppCompatActivity() {
    private lateinit var tk: EditText; private lateinit var ch: EditText
    private lateinit var pin: EditText; private lateinit var tr: EditText
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 48, 32, 32) }
        fun label(s: String) { root.addView(TextView(this).apply { text = s; textSize = 13f }) }
        fun field(hint: String, cur: String): EditText {
            val e = EditText(this).apply { setHint(hint); setText(cur) }
            root.addView(e, LinearLayout.LayoutParams(-1, -2))
            return e
        }
        label("Bot token (do seu @BotFather)");      tk = field("token", Prefs.token(this))
        label("Seu chat id");                        ch = field("chat id", Prefs.chatId(this))
        label("PIN (somente numeros, min 4 digitos)"); pin = field("pin", Prefs.pin(this)).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }
        label("Numero de confianca (SMS, DDI+DDD)"); tr = field("ex 5511998887777", Prefs.trusted(this))

        val btnAdmin = Button(this).apply { text = "1. Ativar Device Admin" }
        btnAdmin.setOnClickListener {
            val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val cn = ComponentName(this, GuardReceiver::class.java)
            if (dpm.isAdminActive(cn)) toast("ja ativo") else
                startActivity(Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, cn))
        }
        val btnPerm = Button(this).apply { text = "2. Conceder permissoes (camera/local/SMS)" }
        btnPerm.setOnClickListener {
            requestPermissions(arrayOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.RECEIVE_SMS, android.Manifest.permission.SEND_SMS,
                android.Manifest.permission.READ_PHONE_STATE,
                android.Manifest.permission.POST_NOTIFICATIONS), 42)
        }
        val sw = Switch(this).apply { text = "3. Protecao armada"; isChecked = Prefs.armed(this@AdminActivity) }
        sw.setOnCheckedChangeListener { _, on ->
            Prefs.set(this@AdminActivity, armed = on)
            if (on) { if (Prefs.ready(this@AdminActivity)) GuardService.start(this@AdminActivity) else { sw.isChecked = false; toast("configure token/chat/pin primeiro") } }
            else GuardService.stop(this@AdminActivity)
            refresh()
        }
        root.addView(sw)
        root.addView(btnAdmin); root.addView(btnPerm)
        status = TextView(this); root.addView(status, LinearLayout.LayoutParams(-1, -2).apply { topMargin = 24 })
        root.addView(Button(this).apply {
            text = "Salvar configuracao"
            setOnClickListener {
                val p = pin.text.toString().trim()
                if (p.length < 4 || !p.all { it.isDigit() }) { toast("PIN invalido: so numeros, min 4 digitos"); return@setOnClickListener }
                Prefs.set(this@AdminActivity, tk.text.toString().trim(), ch.text.toString().trim(),
                          p, tr.text.toString().trim())
                toast("salvo")
                refresh()
            }
        })
        root.addView(Button(this).apply {
            text = "Testar Telegram"
            setOnClickListener {
                Thread {
                    val ok = TgBridge.sendMessage(Prefs.token(this@AdminActivity), Prefs.chatId(this@AdminActivity), "ABLOY OK — canal vivo")
                    runOnUiThread { toast(if (ok) "enviado — olhe o chat do bot" else "falha — confira token/chat") }
                }.start()
            }
        })
        setContentView(root)
        refresh()
    }

    private fun refresh() {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        status.text = "status: admin=${dpm.isAdminActive(ComponentName(this, GuardReceiver::class.java))} " +
                      "armada=${Prefs.armed(this)} pronto=${Prefs.ready(this)}\n" +
                      "checklist: Google Backup ativo (obrigatorio p/ wipe) | SIM PIN | tela de bloqueio"
    }

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}
