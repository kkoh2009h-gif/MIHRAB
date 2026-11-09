package com.mchawe.mihrab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Navy = Color(0xFF101F35)
private val Gold = Color(0xFFC8A96A)
private val Cream = Color(0xFFF6F2E9)
private val Ink = Color(0xFF1C2A3A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MihrabApp() }
    }
}

@Composable
private fun MihrabApp() {
    var selected by remember { mutableIntStateOf(0) }
    val labels = listOf("الرئيسية", "الصلاة", "القرآن", "الأذكار", "المزيد")
    val icons = listOf(Icons.Default.Home, Icons.Default.AccessTime, Icons.Default.MenuBook, Icons.Default.Favorite, Icons.Default.MoreHoriz)

    MaterialTheme(colorScheme = lightColorScheme(
        primary = Navy, secondary = Gold, background = Cream, surface = Color.White
    )) {
        Scaffold(
            containerColor = Cream,
            bottomBar = {
                NavigationBar(containerColor = Color.White) {
                    labels.forEachIndexed { index, label ->
                        NavigationBarItem(
                            selected = selected == index,
                            onClick = { selected = index },
                            icon = { Icon(icons[index], contentDescription = label) },
                            label = { Text(label, fontSize = 10.sp) }
                        )
                    }
                }
            }
        ) { padding ->
            when (selected) {
                0 -> HomeScreen(Modifier.padding(padding))
                else -> PlaceholderScreen(labels[selected], Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun HomeScreen(modifier: Modifier = Modifier) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(1000)
        }
    }
    val time = now.format(DateTimeFormatter.ofPattern("hh:mm:ss a", Locale("ar")))
    val date = now.format(DateTimeFormatter.ofPattern("EEEE، d MMMM yyyy", Locale("ar")))

    Column(
        modifier.fillMaxSize().background(Cream).padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("مِحراب", color = Navy, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("رفيق يومك وعبادتك", color = Ink.copy(alpha = .65f), fontSize = 13.sp)
            }
            Surface(color = Navy, shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Default.Mosque, null, tint = Gold, modifier = Modifier.padding(12.dp).size(28.dp))
            }
        }
        Surface(color = Navy, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("الساعة الآن", color = Gold.copy(alpha = .9f), fontSize = 13.sp)
                Text(time, color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Light)
                Text(date, color = Color.White.copy(alpha = .8f), fontSize = 14.sp)
                Spacer(Modifier.height(16.dp))
                Text("مواقيت الصلاة", color = Gold, fontWeight = FontWeight.SemiBold)
                Text("سيتم ربط المواقيت ببيانات المصدر المعتمد حسب مدينتك", color = Color.White.copy(alpha = .8f), fontSize = 12.sp)
            }
        }
        Text("مساحتك اليومية", color = Navy, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        FeatureTile("وردك اليومي", "تابع قراءتك وختمتك", Icons.Default.MenuBook)
        FeatureTile("أذكار اليوم", "أذكار الصباح والمساء والأدعية", Icons.Default.Favorite)
        FeatureTile("التقويم الهجري", "تابع التاريخ والمناسبات الثابتة", Icons.Default.CalendarMonth)
        Text("الإصدار التأسيسي 0.1.0", color = Ink.copy(alpha = .45f), fontSize = 11.sp)
    }
}

@Composable
private fun FeatureTile(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(color = Color.White, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = Gold.copy(alpha = .16f), shape = RoundedCornerShape(12.dp)) {
                Icon(icon, null, tint = Navy, modifier = Modifier.padding(12.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(title, color = Navy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(subtitle, color = Ink.copy(alpha = .6f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().background(Cream), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Navy, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Text("هذه الصفحة ضمن مراحل التطوير القادمة", color = Ink.copy(alpha = .65f))
        }
    }
}
