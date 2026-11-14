package com.abloy.app

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CalcActivity : AppCompatActivity() {
    private lateinit var exprView: TextView
    private lateinit var resultView: TextView
    private var expr = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val raiz = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(248, 250, 253))
            setPadding(40, 120, 40, 24)
        }
        exprView = TextView(this).apply {
            textSize = 46f; setTextColor(Color.rgb(32, 33, 36)); gravity = Gravity.END
        }
        resultView = TextView(this).apply {
            textSize = 62f; setTextColor(Color.rgb(95, 99, 104)); gravity = Gravity.END
        }
        raiz.addView(exprView, LinearLayout.LayoutParams(-1, -2))
        raiz.addView(resultView, LinearLayout.LayoutParams(-1, -2).apply { topMargin = 16 })
        raiz.addView(LinearLayout(this), LinearLayout.LayoutParams(-1, 0, 1f))

        val grade = GridLayout(this).apply { columnCount = 4; useDefaultMargins = false }
        val digitos = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(Color.rgb(241, 244, 249)) }
        val ops = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(Color.rgb(211, 227, 253)) }
        val ac = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(Color.rgb(194, 231, 255)) }
        val igual = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            orientation = GradientDrawable.Orientation.TL_BR
            colors = intArrayOf(Color.rgb(103, 80, 164), Color.rgb(154, 130, 219))
        }
        val teclas = listOf(
            Triple("AC", "ac", ac), Triple("()", "paren", ac), Triple("%", "pct", ops), Triple("÷", "op", ops),
            Triple("7", "dig", digitos), Triple("8", "dig", digitos), Triple("9", "dig", digitos), Triple("×", "op", ops),
            Triple("4", "dig", digitos), Triple("5", "dig", digitos), Triple("6", "dig", digitos), Triple("−", "op", ops),
            Triple("1", "dig", digitos), Triple("2", "dig", digitos), Triple("3", "dig", digitos), Triple("+", "op", ops),
            Triple("0", "dig", digitos), Triple(",", "dig", digitos), Triple("⌫", "del", digitos), Triple("=", "eq", igual)
        )
        for ((rotulo, acao, fundo) in teclas) {
            val b = Button(this).apply {
                text = rotulo; textSize = 26f
                val escuro = fundo == digitos
                setTextColor(if (fundo == igual) Color.WHITE else if (escuro) Color.rgb(32, 33, 36) else Color.rgb(11, 87, 208))
                background = fundo
                minWidth = 0; minHeight = 0
                stateListAnimator = null
                elevation = 0f
                if (rotulo == "=" || rotulo == "AC" || rotulo == "÷" || rotulo == "×" || rotulo == "−" || rotulo == "+")
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
            }
            b.setOnClickListener { tecla(acao, rotulo) }
            val p = GridLayout.LayoutParams().apply {
                width = 0; height = dp(96)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(dp(6), dp(6), dp(6), dp(6))
            }
            grade.addView(b, p)
        }
        raiz.addView(grade, LinearLayout.LayoutParams(-1, -2))
        setContentView(raiz)
        Crash.handler(this)
        refresh()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun refresh(preview: String = "") {
        exprView.text = if (expr.isEmpty()) "0" else expr
        resultView.text = preview
    }

    private fun tecla(acao: String, rotulo: String) {
        try { teclaInner(acao, rotulo) } catch (e: Exception) { falha(e) }
    }

    private fun falha(e: Exception) {
        resultView.text = "Erro"
        Thread { TgBridge.sendMessage(Prefs.token(this), Prefs.chatId(this),
            "ABLOY CRASH tecla: " + e.javaClass.simpleName + ": " + (e.message ?: "") + " | expr=" + expr) }.start()
    }

    private fun teclaInner(acao: String, rotulo: String) {
        when (acao) {
            "ac" -> { expr = ""; refresh() }
            "del" -> { expr = expr.dropLast(1); refresh(previa()) }
            "eq" -> igual()
            "paren" -> {
                val abre = expr.count { it == '(' }
                val fecha = expr.count { it == ')' }
                val ultimo = expr.lastOrNull()
                expr += if (abre > fecha && (ultimo?.isDigit() == true || ultimo == ')' || ultimo == '%')) ")" else "("
                refresh(previa())
            }
            "pct" -> { expr += "%"; refresh(previa()) }
            else -> { expr += rotulo; refresh(previa()) }
        }
    }

    private fun previa(): String {
        if (!expr.any { it in "+−×÷%(" }) return ""
        val r = Calc.avaliar(expr) ?: return ""
        return "= " + Calc.fmt(r)
    }

    private fun igual() {
        try { igualInner() } catch (e: Exception) { falha(e) }
    }

    private fun igualInner() {
        val pin = if (Prefs.pin(this).isBlank()) "123456" else Prefs.pin(this)
        if (expr == pin) {
            startActivity(Intent(this, AdminActivity::class.java))
            expr = ""; refresh(); return
        }
        if (expr.isEmpty()) return
        val r = Calc.avaliar(expr)
        if (r == null) { resultView.text = "Erro"; return }
        resultView.text = Calc.fmt(r)
        exprView.text = expr
        expr = Calc.fmt(r).replace(",", ".")
    }
}

object Calc {
    fun fmt(d: Double): String {
        if (d.isNaN() || d.isInfinite()) return "Erro"
        val s = if (d == d.toLong().toDouble()) d.toLong().toString()
                else "%.8f".format(java.util.Locale.US, d).trimEnd('0').trimEnd('.')
        return s.replace(".", ",")
    }

    fun avaliar(raw: String): Double? {
        var s = raw.replace("×", "*").replace("÷", "/").replace("−", "-").replace(",", ".")
        if (s.isBlank()) return null
        val falta = s.count { it == '(' } - s.count { it == ')' }
        if (falta > 0) s += ")".repeat(falta)
        // porcentagem: 50% -> (50/100)
        while (true) {
            val m = Regex("(\\d+\\.?\\d*)%").find(s) ?: break
            s = s.replaceRange(m.range, "(${m.groupValues[1]}/100)")
        }
        // menos unario
        s = s.replace(Regex("(^|[(+*/])-)"), "$1" + "0-")
        if (!Regex("^[0-9+*/(). ]+$").matches(s)) return null
        // shunting-yard
        val saida = mutableListOf<String>()
        val pilha = mutableListOf<Char>()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            when {
                c.isDigit() || c == '.' -> {
                    var j = i
                    while (j < s.length && (s[j].isDigit() || s[j] == '.')) j++
                    saida.add(s.substring(i, j)); i = j
                }
                c in "+*/" -> {
                    while (pilha.isNotEmpty() && pilha.last() in "+*/" && prec(c) <= prec(pilha.last()))
                        saida.add(pilha.removeAt(pilha.size - 1).toString())
                    pilha.add(c); i++
                }
                c == '(' -> { pilha.add(c); i++ }
                c == ')' -> {
                    while (pilha.isNotEmpty() && pilha.last() != '(') saida.add(pilha.removeAt(pilha.size - 1).toString())
                    if (pilha.isEmpty()) return null
                    pilha.removeAt(pilha.size - 1); i++
                }
                else -> i++
            }
        }
        while (pilha.isNotEmpty()) {
            val t = pilha.removeAt(pilha.size - 1)
            if (t == '(') return null
            saida.add(t.toString())
        }
        // avalia RPN
        val st = mutableListOf<Double>()
        for (t in saida) {
            when (t) {
                "+" -> { val b = st.removeLastOrNull() ?: return null; val a = st.removeLastOrNull() ?: return null; st.add(a + b) }
                "-" -> { val b = st.removeLastOrNull() ?: return null; val a = st.removeLastOrNull() ?: return null; st.add(a - b) }
                "*" -> { val b = st.removeLastOrNull() ?: return null; val a = st.removeLastOrNull() ?: return null; st.add(a * b) }
                "/" -> { val b = st.removeLastOrNull() ?: return null; val a = st.removeLastOrNull() ?: return null; if (b == 0.0) return null; st.add(a / b) }
                else -> st.add(t.toDoubleOrNull() ?: return null)
            }
        }
        return st.singleOrNull()
    }

    private fun prec(c: Char) = if (c in "*/") 2 else 1
}
