package com.counterflame.companion

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView

class FloatingMenuService : Service() {
    private lateinit var wm: WindowManager
    private var bubble: TextView? = null
    private var panel: LinearLayout? = null
    private var tab = "AIM"
    private val orange = Color.rgb(255, 165, 47)
    private val dark = Color.rgb(18, 23, 31)
    private val card = Color.rgb(29, 37, 48)
    private val muted = Color.rgb(157, 168, 181)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        if (bubble == null) showBubble()
        return START_STICKY
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun bg(color: Int, radius: Int = 14): GradientDrawable =
        GradientDrawable().apply { setColor(color); cornerRadius = dp(radius).toFloat() }

    private fun label(text: String, size: Float = 13f, color: Int = Color.WHITE): TextView =
        TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(color)
            setPadding(dp(10), dp(7), dp(10), dp(7))
        }

    private fun params(width: Int, height: Int, flags: Int): WindowManager.LayoutParams =
        WindowManager.LayoutParams(
            dp(width), if (height < 0) height else dp(height),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            flags, PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START; x = dp(18); y = dp(180) }

    private fun showBubble() {
        val b = label("CF", 16f, Color.BLACK).apply {
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            background = bg(orange, 30)
            setOnClickListener { if (panel == null) showPanel() else hidePanel() }
            setOnTouchListener(object : View.OnTouchListener {
                var sx = 0f; var sy = 0f; var ox = 0; var oy = 0
                override fun onTouch(v: View, e: MotionEvent): Boolean {
                    val lp = v.layoutParams as WindowManager.LayoutParams
                    when (e.action) {
                        MotionEvent.ACTION_DOWN -> { sx = e.rawX; sy = e.rawY; ox = lp.x; oy = lp.y; return true }
                        MotionEvent.ACTION_MOVE -> { lp.x = ox + (e.rawX - sx).toInt(); lp.y = oy + (e.rawY - sy).toInt(); wm.updateViewLayout(v, lp); return true }
                        MotionEvent.ACTION_UP -> { if (kotlin.math.abs(e.rawX - sx) < dp(8) && kotlin.math.abs(e.rawY - sy) < dp(8)) performClick(); return true }
                    }
                    return false
                }
            })
        }
        bubble = b
        wm.addView(b, params(52, 52, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE))
    }

    private fun hidePanel() {
        panel?.let { try { wm.removeView(it) } catch (_: Exception) {} }
        panel = null
    }

    private fun showPanel() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = bg(dark, 18)
            setPadding(dp(10), dp(10), dp(10), dp(10))
        }
        val header = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        header.addView(label("COUNTERFLAME  /  TOOLKIT", 12f, orange), LinearLayout.LayoutParams(0, dp(42), 1f))
        header.addView(Button(this).apply {
            text = "×"; textSize = 18f; setTextColor(Color.WHITE); setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener { hidePanel() }
        }, LinearLayout.LayoutParams(dp(42), dp(42)))
        root.addView(header)
        val tabs = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf("AIM", "VISUALS", "MISC", "CONFIG").forEach { name ->
            tabs.addView(Button(this).apply {
                text = name; textSize = 9f; setTextColor(if (tab == name) orange else Color.WHITE)
                setPadding(0, 0, 0, 0); setBackgroundColor(Color.TRANSPARENT)
                setOnClickListener { tab = name; hidePanel(); showPanel() }
            }, LinearLayout.LayoutParams(0, dp(42), 1f))
        }
        root.addView(tabs)
        root.addView(label("────────────────────────", 10f, muted))
        when (tab) {
            "AIM" -> {
                root.addView(label("AIM TRAINER", 12f, orange))
                root.addView(label("Тренировочные параметры — без автонаведения в матчах.", 11f, muted))
                addSlider(root, "Чувствительность тренировки", "aim_sens", 50, 5)
                addSlider(root, "Размер зоны тренировки", "aim_zone", 60, 30)
                root.addView(label("Практикуй наведение в отдельном тренажёре.", 11f, muted))
            }
            "VISUALS" -> {
                root.addView(label("VISUAL SETTINGS", 12f, orange))
                addSlider(root, "Размер прицела", "cross_size", 40, 20)
                addSlider(root, "Прозрачность", "cross_alpha", 100, 85)
                root.addView(label("Цвет прицела: зелёный (предпросмотр в приложении)", 11f, muted))
            }
            "MISC" -> {
                root.addView(label("MISCELLANEOUS", 12f, orange))
                root.addView(label("• Панель можно перетаскивать за значок CF.\n• Настройки хранятся локально.\n• Не изменяет файлы игры.", 12f, Color.WHITE))
                root.addView(Button(this).apply { text = "Скрыть панель"; setOnClickListener { hidePanel() } })
            }
            else -> {
                root.addView(label("CONFIGURATION", 12f, orange))
                root.addView(Button(this).apply {
                    text = "СОХРАНИТЬ ПРОФИЛЬ"
                    setOnClickListener {
                        getSharedPreferences("cf_config", MODE_PRIVATE).edit().putBoolean("saved", true).apply()
                        text = "СОХРАНЕНО ✓"
                    }
                })
                root.addView(Button(this).apply { text = "ЗАКРЫТЬ СОФТ"; setOnClickListener { stopSelf() } })
            }
        }
        val lp = params(300, -2, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        lp.x = dp(12); lp.y = dp(120)
        panel = root
        wm.addView(root, lp)
    }

    private fun addSlider(parent: LinearLayout, title: String, key: String, max: Int, initial: Int) {
        parent.addView(label(title, 11f, Color.WHITE))
        val pref = getSharedPreferences("cf_config", MODE_PRIVATE)
        val bar = SeekBar(this).apply {
            this.max = max
            progress = pref.getInt(key, initial).coerceIn(0, max)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) pref.edit().putInt(key, progress).apply()
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        parent.addView(bar)
    }

    override fun onDestroy() {
        hidePanel()
        bubble?.let { try { wm.removeView(it) } catch (_: Exception) {} }
        bubble = null
        super.onDestroy()
    }
}
