package com.mchawe.mihrab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.AccessTime,
        Icons.Default.MenuBook,
        Icons.Default.Favorite,
        Icons.Default.MoreHoriz
    )

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
                1 -> PrayerScreen(Modifier.padding(padding))
                2 -> QuranScreen(Modifier.padding(padding))
                3 -> AdhkarScreen(Modifier.padding(padding))
                else -> MoreScreen(Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun PageColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier.fillMaxSize()
            .background(Cream)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content
    )
}

@Composable
private fun PageHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, color = Navy, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Ink.copy(alpha = .65f), fontSize = 13.sp)
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

    PageColumn(modifier) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("مِحراب", color = Navy, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("رفيق يومك وعبادتك", color = Ink.copy(alpha = .65f), fontSize = 13.sp)
            }
            Surface(color = Navy, shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Default.AutoAwesome, null, tint = Gold, modifier = Modifier.padding(12.dp).size(28.dp))
            }
        }
        Surface(color = Navy, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("الساعة الآن", color = Gold.copy(alpha = .9f), fontSize = 13.sp)
                Text(time, color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Light)
                Text(date, color = Color.White.copy(alpha = .8f), fontSize = 14.sp)
                Spacer(Modifier.height(16.dp))
                Text("مواقيت الصلاة", color = Gold, fontWeight = FontWeight.SemiBold)
                Text(
                    "سيتم عرض المواقيت بعد ربط مصدر البيانات والتحقق منها لمدينتك",
                    color = Color.White.copy(alpha = .8f), fontSize = 12.sp
                )
            }
        }
        Text("مساحتك اليومية", color = Navy, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        FeatureTile("وردك اليومي", "تابع قراءتك وختمتك", Icons.Default.MenuBook)
        FeatureTile("أذكار اليوم", "أذكار الصباح والمساء والأدعية", Icons.Default.Favorite)
        FeatureTile("التقويم الهجري", "تابع التاريخ والمناسبات الثابتة", Icons.Default.CalendarMonth)
        Text("مِحراب • الإصدار 0.2.0", color = Ink.copy(alpha = .45f), fontSize = 11.sp)
    }
}

@Composable
private fun PrayerScreen(modifier: Modifier = Modifier) {
    val cities = listOf("كركوك", "بغداد", "الموصل", "أربيل", "السليمانية", "دهوك", "البصرة", "النجف", "كربلاء", "الأنبار")
    var city by remember { mutableStateOf("كركوك") }
    var expanded by remember { mutableStateOf(false) }
    PageColumn(modifier) {
        PageHeader("مواقيت الصلاة", "المواقيت الخاصة بمدينتك")
        Surface(color = Navy, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("المدينة المحددة", color = Gold, fontWeight = FontWeight.Bold)
                Text(city, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                Box {
                    OutlinedButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.LocationCity, null)
                        Spacer(Modifier.width(8.dp))
                        Text("اختيار المدينة")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        cities.forEach { item ->
                            DropdownMenuItem(text = { Text(item) }, onClick = {
                                city = item
                                expanded = false
                            })
                        }
                    }
                }
                Text("سيتم ربط المدينة بالموقع الجغرافي ومصدر المواقيت الرسمي في الخطوة التالية.", color = Color.White.copy(alpha = .75f), fontSize = 12.sp)
            }
        }
        Text("جدول اليوم", color = Navy, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        listOf("الفجر", "الشروق", "الظهر", "العصر", "المغرب", "العشاء").forEach { prayer ->
            Surface(color = Color.White, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(prayer, color = Navy, fontWeight = FontWeight.SemiBold)
                    Text("بانتظار البيانات الموثقة", color = Ink.copy(alpha = .55f), fontSize = 12.sp)
                }
            }
        }
        Text("لن نعرض أوقاتاً تقديرية على أنها مواقيت رسمية.", color = Ink.copy(alpha = .7f), fontSize = 12.sp)
    }
}

@Composable
private fun QuranScreen(modifier: Modifier = Modifier) {
    PageColumn(modifier) {
        PageHeader("القرآن الكريم", "مصحف بصفحات واضحة وقراءة مريحة")
        Surface(color = Color.White, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.MenuBook, null, tint = Gold, modifier = Modifier.size(42.dp))
                Text("المصحف الشريف", color = Navy, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    "نعمل على إضافة نص قرآني موثّق بالرسم العثماني وتخطيط صفحات مطابق لمصحف معتمد.",
                    color = Ink.copy(alpha = .7f), fontSize = 13.sp
                )
                Text("لن نستخدم نصوصاً تجريبية أو آيات غير مدققة.", color = Navy, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        FeatureTile("علامات القراءة", "حفظ موضع القراءة والعلامات المرجعية", Icons.Default.Bookmark)
        FeatureTile("ختمة القرآن", "خطط ختمة 7 أو 15 أو 30 يوماً", Icons.Default.CheckCircle)
    }
}

@Composable
private fun AdhkarScreen(modifier: Modifier = Modifier) {
    PageColumn(modifier) {
        PageHeader("الأذكار والأدعية", "أذكار ثابتة مع مصادرها")
        FeatureTile("أذكار الصباح", "نصوص موثقة وعدّاد للتكرار", Icons.Default.WbSunny)
        FeatureTile("أذكار المساء", "اقرأ أذكار المساء واحفظ تقدمك", Icons.Default.DarkMode)
        FeatureTile("أذكار بعد الصلاة", "أذكار صحيحة مع المراجع", Icons.Default.Favorite)
        Text("سيتم نشر الأذكار بعد مراجعة النصوص والمراجع، دون نسبة نص غير موثّق.", color = Ink.copy(alpha = .7f), fontSize = 12.sp)
    }
}

@Composable
private fun MoreScreen(modifier: Modifier = Modifier) {
    PageColumn(modifier) {
        PageHeader("المزيد", "إعدادات وأدوات مِحراب")
        FeatureTile("التقويم الهجري", "التاريخ الهجري والمناسبات الثابتة", Icons.Default.CalendarMonth)
        FeatureTile("التنبيهات والأذان", "إعدادات التنبيه والصوت لكل صلاة", Icons.Default.Notifications)
        FeatureTile("الإعدادات", "المظهر واللغة والموقع وطريقة عرض الوقت", Icons.Default.Settings)
        Text("مِحراب — تطبيق عربي يحترم خصوصيتك ويعمل دون خدمات سحابية مدفوعة.", color = Ink.copy(alpha = .65f), fontSize = 12.sp)
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
