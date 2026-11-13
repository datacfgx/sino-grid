package com.abloy.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CalcActivity : AppCompatActivity() {
    private lateinit var display: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(32, 64, 32, 32)
        }
        display = TextView(this).apply {
            textSize = 34f; text = "0"; gravity = android.view.Gravity.END
            setPadding(0, 0, 24, 48)
        }
        root.addView(display, android.widget.LinearLayout.LayoutParams(-1, -2))
        val grid = GridLayout(this).apply { columnCount = 4 }
        val keys = listOf("7","8","9","÷","4","5","6","×","1","2","3","-","0",".","C","+","=")
        for (k in keys) {
            val b = Button(this).apply { text = k; textSize = 22f }
            b.setOnClickListener { press(k) }
            grid.addView(b, GridLayout.LayoutParams().apply { width = 0; height = -2; columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f) })
        }
        root.addView(grid, android.widget.LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun press(k: String) {
        val cur = display.text.toString()
        when (k) {
            "C" -> display.text = "0"
            "=" -> equals(cur)
            else -> display.text = if (cur == "0") k else cur + k
        }
    }

    private fun equals(cur: String) {
        val pin = if (Prefs.pin(this).isBlank()) "123456" else Prefs.pin(this)
        if (cur == pin) {
            startActivity(Intent(this, AdminActivity::class.java))
            display.text = "0"
            return
        }
        val norm = cur.replace("×", "*").replace("÷", "/").replace("x", "*").replace("X", "*")
        val m = Regex("^(-?\\d+\\.?\\d*)([+\\-*/])(-?\\d+\\.?\\d*)$").find(norm)
        display.text = if (m != null) {
            val a = m.groupValues[1].toDoubleOrNull() ?: return invalid()
            val b = m.groupValues[3].toDoubleOrNull() ?: return invalid()
            val r = when (m.groupValues[2]) {
                "+" -> a + b; "-" -> a - b; "*" -> a * b; "/" -> if (b != 0.0) a / b else return invalid()
                else -> return invalid()
            }
            if (r == r.toLong().toDouble()) r.toLong().toString() else r.toString()
        } else if (Regex("^-?\\d+\\.?\\d*$").matches(norm)) norm
        else "Erro"
    }

    private fun invalid(): Unit { display.text = "Erro" }
}
