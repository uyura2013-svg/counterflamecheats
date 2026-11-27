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
    private val prefs by lazy { getSharedPreferences("cf_config", MODE_PRIVATE) }

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

    private fun addButton(parent: LinearLayout, title: String, action: () -> Unit) {
        parent.addView(Button(this).apply {
            text = title
            setTextColor(Color.WHITE)
            setOnClickListener { action() }
        })
    }

    private fun addToggle(parent: LinearLayout, title: String, key: String, note: String? = null) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = bg(card, 10)
            setPadding(dp(7), dp(4), dp(7), dp(4))
        }
        val toggle = android.widget.Switch(this).apply {
            text = title
            textSize = 12f
            setTextColor(Color.WHITE)
            isChecked = prefs.getBoolean(key, false)
            setOnCheckedChangeListener { _, checked -> prefs.edit().putBoolean(key, checked).apply() }
        }
        row.addView(toggle)
        if (note != null) row.addView(label(note, 10f, muted))
        parent.addView(row, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(6) })
    }

    private fun showPanel() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = bg(dark, 18)
            setPadding(dp(10), dp(10), dp(10), dp(10))
        }
        val header = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        header.addView(label("COUNTERFLAME / TOOLKIT", 12f, orange), LinearLayout.LayoutParams(0, dp(42), 1f))
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
                root.addView(label("AIM SETTINGS", 12f, orange))
                root.addView(label("Настройки доступны для отдельного офлайн-тренажёра. В матчах наведение не меняется.", 10f, muted))
                addToggle(root, "Aim Assist — тренировка", "aim_assist", "Переключатель режима тренировки, не управляет прицелом в игре.")
                addSlider(root, "FOV тренировки", "aim_fov", 180, 90)
                addSlider(root, "Smooth тренировки", "aim_smooth", 100, 35)
                addSlider(root, "Чувствительность", "aim_sens", 100, 50)
                addToggle(root, "Удерживать Aim Key (демо)", "aim_key", "Сохраняет предпочтение в профиле.")
                root.addView(label("Silent Aim: недоступен — не добавляю скрытое наведение или вмешательство в игру.", 10f, muted))
            }
            "VISUALS" -> {
                root.addView(label("VISUAL SETTINGS", 12f, orange))
                root.addView(label("Демо-переключатели интерфейса; не показывают игроков и объекты из матча.", 10f, muted))
                addToggle(root, "ESP — демо-индикатор", "vis_esp", "Только локальный пример в меню.")
                addToggle(root, "Skeleton — демо-схема", "vis_skeleton", "Не отслеживает игровые модели.")
                addToggle(root, "Box — демо-рамка", "vis_box", "Не рисует рамки вокруг игроков в матче.")
                addToggle(root, "Crosshair — предпросмотр", "vis_crosshair")
                addSlider(root, "Размер прицела", "cross_size", 60, 30)
                addSlider(root, "Прозрачность", "cross_alpha", 100, 85)
                addSlider(root, "Толщина линий", "cross_thickness", 20, 3)
                addToggle(root, "Лёгкий режим оверлея", "low_load", "Упрощает только интерфейс CounterFlame Toolkit.")
            }
            "MISC" -> {
                root.addView(label("BASIC FUNCTIONS", 12f, orange))
                addToggle(root, "Компактная панель", "compact_ui", "Сохраняет выбор режима интерфейса.")
                addToggle(root, "Запоминать настройки", "remember_settings", "Настройки сохраняются на устройстве.")
                addButton(root, "СВЕРНУТЬ ПАНЕЛЬ") { hidePanel() }
                addButton(root, "ПЕРЕМЕСТИТЬ ПУЗЫРЁК В УГОЛ") {
                    bubble?.let {
                        val lp = it.layoutParams as WindowManager.LayoutParams
                        lp.x = dp(12); lp.y = dp(100); wm.updateViewLayout(it, lp)
                    }
                }
                root.addView(label("Пузырёк CF можно перетаскивать. Нажми его, чтобы открыть или скрыть меню.", 10f, muted))
                root.addView(label("Приложение не читает память игры, не внедряет код и не меняет файлы CounterFlame.", 10f, muted))
            }
            else -> {
                root.addView(label("CONFIGURATION", 12f, orange))
                addButton(root, "СОХРАНИТЬ ПРОФИЛЬ") {
                    val snapshot = getSharedPreferences("cf_profile", MODE_PRIVATE).edit()
                    prefs.all.forEach { (key, value) ->
                        when (value) {
                            is Boolean -> snapshot.putBoolean(key, value)
                            is Int -> snapshot.putInt(key, value)
                            is Float -> snapshot.putFloat(key, value)
                            is Long -> snapshot.putLong(key, value)
                            is String -> snapshot.putString(key, value)
                        }
                    }
                    snapshot.apply()
                    tab = "CONFIG"; hidePanel(); showPanel()
                }
                addButton(root, "ЗАГРУЗИТЬ ПРОФИЛЬ") {
                    val saved = getSharedPreferences("cf_profile", MODE_PRIVATE)
                    if (saved.all.isNotEmpty()) {
                        val edit = prefs.edit().clear()
                        saved.all.forEach { (key, value) ->
                            when (value) {
                                is Boolean -> edit.putBoolean(key, value)
                                is Int -> edit.putInt(key, value)
                                is Float -> edit.putFloat(key, value)
                                is Long -> edit.putLong(key, value)
                                is String -> edit.putString(key, value)
                            }
                        }
                        edit.apply()
                        hidePanel(); showPanel()
                    }
                }
                addButton(root, "СБРОСИТЬ НАСТРОЙКИ") {
                    prefs.edit().clear().apply()
                    hidePanel(); showPanel()
                }
                addButton(root, "ЗАКРЫТЬ СОФТ") { stopSelf() }
                root.addView(label("Профиль хранится локально на устройстве.", 10f, muted))
            }
        }
        val lp = params(310, -2, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        lp.x = dp(12); lp.y = dp(100)
        panel = root
        wm.addView(root, lp)
    }

    private fun addSlider(parent: LinearLayout, title: String, key: String, max: Int, initial: Int) {
        parent.addView(label("$title: ${prefs.getInt(key, initial)}", 11f, Color.WHITE))
        val valueLabel = parent.getChildAt(parent.childCount - 1) as TextView
        val bar = SeekBar(this).apply {
            this.max = max
            progress = prefs.getInt(key, initial).coerceIn(0, max)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    valueLabel.text = "$title: $progress"
                    if (fromUser) prefs.edit().putInt(key, progress).apply()
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
