package com.counterflame.companion

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.hypot
import kotlin.random.Random

private val Bg = Color(0xFF0D1117)
private val Panel = Color(0xFF171E27)
private val Accent = Color(0xFFFFA52F)
private val Muted = Color(0xFF9AA5B1)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var trainerOpen by remember { mutableStateOf(false) }
            MaterialTheme(colorScheme = darkColorScheme(primary = Accent, background = Bg, surface = Panel)) {
                if (trainerOpen) {
                    AimTrainerScreen(onBack = { trainerOpen = false })
                } else {
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
                                Text("Aim — офлайн-тренировка и настройки практики. Visuals — предпросмотр прицела. Misc — параметры интерфейса. Config — профили.", color = Color.White, lineHeight = 21.sp)
                                Text("Приложение не читает память CounterFlame, не управляет персонажем и не меняет игровые файлы.", color = Muted, fontSize = 12.sp)
                            }
                        }
                        Button(
                            onClick = { trainerOpen = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Panel)
                        ) { Text("ОТКРЫТЬ AIM-ТРЕНАЖЁР", color = Accent, fontWeight = FontWeight.Bold) }
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
                        Text("Для плавающей панели разреши отображение поверх других приложений в настройках Android, затем вернись сюда.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
                        Spacer(Modifier.weight(1f))
                        Text("COUNTERFLAME TOOLKIT · v2.0", color = Muted, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }
            }
        }
    }
}

@Composable
private fun AimTrainerScreen(onBack: () -> Unit) {
    var targetX by remember { mutableFloatStateOf(0.5f) }
    var targetY by remember { mutableFloatStateOf(0.5f) }
    var hits by remember { mutableIntStateOf(0) }
    var shots by remember { mutableIntStateOf(0) }
    val accuracy = if (shots == 0) 0 else (hits * 100 / shots)

    Column(
        Modifier.fillMaxSize().background(Bg).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("←", color = Accent, fontSize = 24.sp, modifier = Modifier.clickable { onBack() }.padding(end = 14.dp))
            Text("AIM TRAINER", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
        }
        Text("Офлайн-практика реакции и точности. Нажимай на появляющуюся мишень.", color = Muted, fontSize = 13.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Stat("ПОПАДАНИЯ", hits.toString())
            Stat("ПОПЫТКИ", shots.toString())
            Stat("ТОЧНОСТЬ", "$accuracy%")
        }
        Card(
            Modifier.fillMaxWidth().weight(1f),
            colors = CardDefaults.cardColors(containerColor = Panel),
            shape = RoundedCornerShape(18.dp)
        ) {
            Canvas(
                Modifier.fillMaxSize().pointerInput(targetX, targetY) {
                    detectTapGestures { point ->
                        shots++
                        val cx = size.width * targetX
                        val cy = size.height * targetY
                        val radius = size.minDimension * 0.075f
                        if (hypot(point.x - cx, point.y - cy) <= radius) hits++
                        targetX = Random.nextFloat() * 0.68f + 0.16f
                        targetY = Random.nextFloat() * 0.68f + 0.16f
                    }
                }
            ) {
                val center = Offset(size.width * targetX, size.height * targetY)
                val radius = size.minDimension * 0.075f
                drawCircle(Accent.copy(alpha = 0.18f), radius * 1.65f, center)
                drawCircle(Accent, radius, center)
                drawCircle(Bg, radius * 0.62f, center)
                drawCircle(Accent, radius * 0.23f, center)
                drawLine(Color.White.copy(alpha = 0.8f), Offset(center.x - radius * 1.8f, center.y), Offset(center.x + radius * 1.8f, center.y), 2f)
                drawLine(Color.White.copy(alpha = 0.8f), Offset(center.x, center.y - radius * 1.8f), Offset(center.x, center.y + radius * 1.8f), 2f)
            }
        }
        Text("Тренажёр работает автономно и не подключается к игровому процессу.", color = Muted, fontSize = 11.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { hits = 0; shots = 0; targetX = 0.5f; targetY = 0.5f }, modifier = Modifier.weight(1f)) {
                Text("СБРОСИТЬ")
            }
            Button(onClick = onBack, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Accent)) {
                Text("В МЕНЮ", color = Bg)
            }
        }
    }
}

@Composable
private fun Stat(title: String, value: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 9.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Muted, fontSize = 9.sp)
            Text(value, color = Accent, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        }
    }
}
