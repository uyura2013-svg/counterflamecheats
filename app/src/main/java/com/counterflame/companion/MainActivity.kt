package com.counterflame.companion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF101318)
private val Panel = Color(0xFF1A2028)
private val Accent = Color(0xFFFFA52F)
private val Muted = Color(0xFF9AA5B1)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CompanionApp() }
    }
}

@Composable
fun CompanionApp() {
    var nickname by remember { mutableStateOf("Player") }
    var matches by remember { mutableIntStateOf(0) }
    var wins by remember { mutableIntStateOf(0) }
    var sensitivity by remember { mutableFloatStateOf(2.5f) }
    var selected by remember { mutableStateOf("Классический") }
    var notes by remember { mutableStateOf("") }

    MaterialTheme(colorScheme = darkColorScheme(
        background = Bg, surface = Panel, primary = Accent,
        onBackground = Color.White, onSurface = Color.White
    )) {
        Column(
            Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState()).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("COUNTERFLAME", color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
            Text("COMPANION", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
            Text("Личный помощник игрока · без вмешательства в игру", color = Muted, fontSize = 13.sp)

            CardBlock("ПРОФИЛЬ") {
                OutlinedTextField(value = nickname, onValueChange = { nickname = it }, label = { Text("Никнейм") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
            CardBlock("СТАТИСТИКА МАТЧЕЙ") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatBox("Матчи", matches.toString(), Modifier.weight(1f))
                    StatBox("Победы", wins.toString(), Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { matches++ }, colors = ButtonDefaults.buttonColors(containerColor = Accent)) { Text("+ Матч", color = Bg) }
                    OutlinedButton(onClick = { if (matches > 0) { matches--; if (wins > matches) wins = matches } }) { Text("− Матч") }
                    OutlinedButton(onClick = { if (wins < matches) wins++ }) { Text("+ Победа") }
                }
            }
            CardBlock("ЧУВСТВИТЕЛЬНОСТЬ · ЗАМЕТКА") {
                Text(String.format("%.1f", sensitivity), color = Accent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Slider(value = sensitivity, onValueChange = { sensitivity = it }, valueRange = 0.1f..10f)
                Text("Это только сохранённая памятка — приложение не меняет настройки игры.", color = Muted, fontSize = 12.sp)
            }
            CardBlock("ПРЕДПРОСМОТР ПРИЦЕЛА") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Классический", "Точка", "Тонкий").forEach { item ->
                        FilterChip(selected = selected == item, onClick = { selected = item }, label = { Text(item) })
                    }
                }
                Box(Modifier.fillMaxWidth().height(100.dp).background(Color(0xFF0B0E12), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    when (selected) {
                        "Точка" -> Box(Modifier.size(6.dp).background(Color(0xFF67E58B), RoundedCornerShape(50)))
                        "Тонкий" -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.width(2.dp).height(18.dp).background(Color(0xFF67E58B)))
                            Box(Modifier.width(18.dp).height(2.dp).background(Color(0xFF67E58B)))
                            Box(Modifier.width(2.dp).height(18.dp).background(Color(0xFF67E58B)))
                        }
                        else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.width(2.dp).height(16.dp).background(Color(0xFF67E58B)))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.width(16.dp).height(2.dp).background(Color(0xFF67E58B)))
                                Box(Modifier.size(3.dp).background(Color(0xFF67E58B), RoundedCornerShape(50)))
                                Box(Modifier.width(16.dp).height(2.dp).background(Color(0xFF67E58B)))
                            }
                            Box(Modifier.width(2.dp).height(16.dp).background(Color(0xFF67E58B)))
                        }
                    }
                }
            }
            CardBlock("ЗАМЕТКИ") {
                OutlinedTextField(value = notes, onValueChange = { notes = it }, modifier = Modifier.fillMaxWidth(), minLines = 3, placeholder = { Text("Тактика, настройки, идеи…") })
            }
            CardBlock("ПОДСКАЗКИ ПО ПРОИЗВОДИТЕЛЬНОСТИ") {
                Text("• Закрой фоновые приложения перед матчем.\n• Включи игровой режим телефона, если он доступен.\n• Подбирай графику так, чтобы игра работала стабильно.", color = Muted, fontSize = 13.sp, lineHeight = 20.sp)
            }
            Text("Версия 1.0 · Companion only", color = Muted, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
private fun CardBlock(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(16.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        content()
    }
}

@Composable
private fun StatBox(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.background(Color(0xFF11161D), RoundedCornerShape(12.dp)).padding(12.dp)) {
        Text(value, color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Muted, fontSize = 12.sp)
    }
}
