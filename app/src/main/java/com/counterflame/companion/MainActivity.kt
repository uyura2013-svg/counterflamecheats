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
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("COUNTERFLAME", color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                    Text("TOOLKIT", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
                    Text("Панель локальных настроек для Android", color = Muted)

                    Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("МЕНЮ НАСТРОЕК", color = Accent, fontWeight = FontWeight.Bold)
                            Text("Aim — только параметры отдельной тренировки. Visuals — предпросмотр прицела и интерфейса. Config — локальные профили.", color = Color.White, lineHeight = 20.sp)
                            Text("Приложение не читает память CounterFlame, не меняет игровые файлы и не управляет прицелом в матчах.", color = Muted, fontSize = 12.sp)
                        }
                    }

                    Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("FPS / PERFORMANCE", color = Accent, fontWeight = FontWeight.Bold)
                            Text("Само приложение не может безопасно разогнать CounterFlame или изменить его FPS. Но можно убрать лишнюю нагрузку:", color = Color.White, lineHeight = 20.sp)
                            Text("• Закрой плавающее меню перед запуском матча.\n• В игре снизь качество графики и эффекты.\n• Выбери стабильный лимит FPS, который телефон держит без перегрева.\n• Закрой фоновые приложения и отключи энергосбережение на время игры.\n• Не играй во время зарядки, если телефон сильно нагревается.", color = Muted, fontSize = 12.sp, lineHeight = 19.sp)
                            OutlinedButton(
                                onClick = { stopService(Intent(this@MainActivity, FloatingMenuService::class.java)) },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("ЗАКРЫТЬ ОВЕРЛЕЙ ПЕРЕД МАТЧЕМ") }
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

                    Text("Для меньшей нагрузки оставляй оверлей выключенным во время игры. После разрешения Android вернись сюда и нажми запуск.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
                    Spacer(Modifier.weight(1f))
                    Text("COUNTERFLAME TOOLKIT · v2.1", color = Muted, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        }
    }
}
