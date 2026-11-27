package com.counterflame.companion

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF0D1117)
private val Panel = Color(0xFF171E27)
private val Accent = Color(0xFFFFA52F)
private val Muted = Color(0xFF9AA5B1)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Accent, background = Bg, surface = Panel)) {
                Column(
                    Modifier.fillMaxSize().background(Bg).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("COUNTERFLAME", color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                    Text("FLOAT MENU", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
                    Text("Плавающая панель настроек для Android", color = Muted)
                    Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("МЕНЮ СОФТА", color = Accent, fontWeight = FontWeight.Bold)
                            Text("Aim — настройки тренировки и практики. Visuals — настройки интерфейса. Misc — дополнительные параметры. Config — сохранение профиля.", color = Color.White, lineHeight = 21.sp)
                            Text("Панель отображается поверх приложений. Она не читает память CounterFlame и не управляет игровым персонажем.", color = Muted, fontSize = 12.sp)
                        }
                    }
                    Button(
                        onClick = {
                            if (!Settings.canDrawOverlays(this@MainActivity)) {
                                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                            } else {
                                startService(Intent(this@MainActivity, FloatingMenuService::class.java))
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent)
                    ) { Text("ЗАПУСТИТЬ ПЛАВАЮЩЕЕ МЕНЮ", color = Bg, fontWeight = FontWeight.Bold) }
                    OutlinedButton(
                        onClick = { stopService(Intent(this@MainActivity, FloatingMenuService::class.java)) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("ОСТАНОВИТЬ МЕНЮ") }
                    Text("Если Android откроет настройки разрешений, разреши отображение поверх других приложений, затем вернись сюда и нажми запуск ещё раз.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
                    Spacer(Modifier.weight(1f))
                    Text("COUNTERFLAME TOOLKIT · v2.0", color = Muted, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (Settings.canDrawOverlays(this)) {
            // The user can launch the overlay with the button after granting permission.
        }
    }
}
