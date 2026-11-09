package com.mchawe.mihrab

import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
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
    val hijriDate = HijrahDate.from(now.toLocalDate())
    val hijriMonths = listOf("محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة")
    val hijriText = "${hijriDate.get(ChronoField.DAY_OF_MONTH)} ${hijriMonths[hijriDate.get(ChronoField.MONTH_OF_YEAR) - 1]} ${hijriDate.get(ChronoField.YEAR)}"

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
                Text("التاريخ الهجري: $hijriText هـ", color = Gold, fontSize = 13.sp)
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
        Text("مِحراب • الإصدار 0.8.0", color = Ink.copy(alpha = .45f), fontSize = 11.sp)
    }
}

@Composable
private fun PrayerScreen(modifier: Modifier = Modifier) {
    val cities = listOf("كركوك", "بغداد", "الموصل", "أربيل", "السليمانية", "دهوك", "البصرة", "النجف", "كربلاء", "الأنبار")
    val context = LocalContext.current
    var city by remember { mutableStateOf(context.getSharedPreferences("mihrab", Context.MODE_PRIVATE).getString("city", "كركوك") ?: "كركوك") }
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
                                context.getSharedPreferences("mihrab", Context.MODE_PRIVATE).edit().putString("city", item).apply()
                                expanded = false
                            })
                        }
                    }
                }
                Text("المصدر المعتمد للمراجعة: موقع ديوان الوقف السني العراقي.", color = Color.White.copy(alpha = .85f), fontSize = 12.sp)
                OutlinedButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://dws.gov.iq/"))
                    context.startActivity(intent)
                }) {
                    Icon(Icons.Default.OpenInNew, null)
                    Spacer(Modifier.width(8.dp))
                    Text("فتح موقع ديوان الوقف السني")
                }
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
        Text("لم نتحقق بعد من واجهة بيانات رسمية منفصلة لكل مدينة؛ لذلك لن ننسخ مواقيت عامة أو نحسب أوقاتاً بديلة على أنها صادرة عن الديوان. افتح الموقع الرسمي للمراجعة إلى أن يكتمل الربط والتحقق.", color = Ink.copy(alpha = .7f), fontSize = 12.sp)
    }
}

@Composable
private fun QuranScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mihrab", Context.MODE_PRIVATE) }
    var targetDays by remember { mutableIntStateOf(prefs.getInt("khatmah_days", 30)) }
    var completedPages by remember { mutableIntStateOf(prefs.getInt("khatmah_pages", 0).coerceIn(0, 604)) }
    var currentPage by remember { mutableIntStateOf(prefs.getInt("quran_current_page", 1).coerceIn(1, 604)) }
    var savedPage by remember { mutableIntStateOf(prefs.getInt("quran_saved_page", 1).coerceIn(1, 604)) }
    val dailyPages = kotlin.math.ceil(604.0 / targetDays).toInt()
    val progress = completedPages / 604f
    PageColumn(modifier) {
        PageHeader("القرآن الكريم", "مصحف بصفحات واضحة وقراءة مريحة")
        Surface(color = Color.White, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.MenuBook, null, tint = Gold, modifier = Modifier.size(42.dp))
                Text("المصحف الشريف", color = Navy, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("نعمل على إضافة نص قرآني موثّق بالرسم العثماني وتخطيط صفحات مطابق لمصحف معتمد.", color = Ink.copy(alpha = .7f), fontSize = 13.sp)
                Text("لن نستخدم نصوصاً تجريبية أو آيات غير مدققة.", color = Navy, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Text("خطة ختم القرآن", color = Navy, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Surface(color = Color.White, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("اختر مدة الختمة", color = Navy, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(7, 15, 30).forEach { days ->
                        FilterChip(selected = targetDays == days, onClick = { targetDays = days; prefs.edit().putInt("khatmah_days", days).apply() }, label = { Text("$days يوم") })
                    }
                }
                Text("الإنجاز: $completedPages من 604 صفحة", color = Navy, fontWeight = FontWeight.SemiBold)
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), color = Gold)
                Text("المعدل التقريبي: $dailyPages صفحة يومياً", color = Ink.copy(alpha = .7f), fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = { completedPages = (completedPages + 1).coerceAtMost(604); prefs.edit().putInt("khatmah_pages", completedPages).apply() }, enabled = completedPages < 604, colors = ButtonDefaults.buttonColors(containerColor = Navy)) { Text("أكملت صفحة") }
                    OutlinedButton(onClick = { completedPages = 0; prefs.edit().putInt("khatmah_pages", 0).apply() }) { Text("إعادة الختمة") }
                }
                if (completedPages == 604) Text("ما شاء الله، أكملت 604 صفحة. تقبّل الله منك.", color = Navy, fontWeight = FontWeight.Bold)
            }
        }
        Text("موضع القراءة", color = Navy, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Surface(color = Color.White, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("الصفحة الحالية", color = Ink.copy(alpha = .65f), fontSize = 13.sp)
                Text("$currentPage / 604", color = Navy, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                LinearProgressIndicator(
                    progress = { currentPage / 604f },
                    modifier = Modifier.fillMaxWidth(),
                    color = Gold
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedButton(
                        onClick = {
                            currentPage = (currentPage - 1).coerceAtLeast(1)
                            prefs.edit().putInt("quran_current_page", currentPage).apply()
                        },
                        enabled = currentPage > 1
                    ) { Text("الصفحة السابقة") }
                    Button(
                        onClick = {
                            currentPage = (currentPage + 1).coerceAtMost(604)
                            prefs.edit().putInt("quran_current_page", currentPage).apply()
                        },
                        enabled = currentPage < 604,
                        colors = ButtonDefaults.buttonColors(containerColor = Navy)
                    ) { Text("الصفحة التالية") }
                }
                Divider(color = Cream)
                Text("آخر موضع محفوظ: صفحة $savedPage", color = Navy, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            savedPage = currentPage
                            prefs.edit().putInt("quran_saved_page", savedPage).apply()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Navy)
                    ) { Icon(Icons.Default.Bookmark, null); Spacer(Modifier.width(6.dp)); Text("حفظ الموضع") }
                    OutlinedButton(onClick = {
                        currentPage = savedPage
                        prefs.edit().putInt("quran_current_page", currentPage).apply()
                    }) { Text("استرجاع المحفوظ") }
                }
                Text("يُحفظ موضعك على الجهاز حتى بعد إغلاق التطبيق.", color = Ink.copy(alpha = .6f), fontSize = 12.sp)
            }
        }
    }
}
@Composable
private fun AdhkarScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mihrab", Context.MODE_PRIVATE) }
    var count by remember { mutableIntStateOf(prefs.getInt("tasbih_count", 0)) }
    var selected by remember { mutableIntStateOf(prefs.getInt("tasbih_phrase", 0).coerceIn(0, 2)) }
    val phrases = listOf("سبحان الله", "الحمد لله", "الله أكبر")
    PageColumn(modifier) {
        PageHeader("الأذكار والأدعية", "أذكار موثقة وعدّاد شخصي")
        Surface(color = Navy, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("العدّاد الشخصي", color = Gold, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    phrases.forEachIndexed { index, phrase ->
                        FilterChip(selected = selected == index, onClick = { selected = index; prefs.edit().putInt("tasbih_phrase", index).apply() }, label = { Text(phrase) })
                    }
                }
                Text(phrases[selected], color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(count.toString(), color = Gold, fontSize = 48.sp, fontWeight = FontWeight.Light)
                Button(onClick = { count += 1; prefs.edit().putInt("tasbih_count", count).apply() }, colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy), modifier = Modifier.fillMaxWidth()) { Text("تسبيحة", fontSize = 18.sp) }
                OutlinedButton(onClick = { count = 0; prefs.edit().putInt("tasbih_count", 0).apply() }) { Text("تصفير العدّاد", color = Color.White) }
            }
        }
        Text("أذكار بعد الصلاة المفروضة", color = Navy, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        DhikrProgressCard(prefs, "after_prayer_istighfar", "أستغفر الله", "صحيح مسلم، حديث 591", 3)
        DhikrProgressCard(prefs, "after_prayer_salam", "اللهم أنت السلام ومنك السلام تباركت يا ذا الجلال والإكرام", "صحيح مسلم، حديث 591", 1)
        Text("أذكار التسبيح بعد الصلاة", color = Navy, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        DhikrProgressCard(prefs, "after_prayer_subhanallah", "سبحان الله", "صحيح مسلم، حديث 597", 33)
        DhikrProgressCard(prefs, "after_prayer_alhamdulillah", "الحمد لله", "صحيح مسلم، حديث 597", 33)
        DhikrProgressCard(prefs, "after_prayer_allahuakbar", "الله أكبر", "صحيح مسلم، حديث 597", 33)
        DhikrProgressCard(prefs, "after_prayer_tahlil", "لا إله إلا الله وحده لا شريك له، له الملك وله الحمد وهو على كل شيء قدير", "صحيح مسلم، حديث 597 — تمام المئة", 1)
        Text("أذكار الصباح والمساء", color = Navy, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        DhikrCounterCard(prefs, "dhikr_morning_evening_1", "دعاء الصباح والمساء",
            "اللهم بك أصبحنا، وبك أمسينا، وبك نحيا، وبك نموت، وإليك النشور.",
            "اللهم بك أمسينا، وبك نحيا، وبك نموت، وإليك النشور.",
            "سنن أبي داود 5068 — صححه الألباني", 1)
        DhikrCounterCard(prefs, "dhikr_morning_evening_2", "باسم الله الذي لا يضر",
            "بسم الله الذي لا يضر مع اسمه شيء في الأرض ولا في السماء وهو السميع العليم.",
            "بسم الله الذي لا يضر مع اسمه شيء في الأرض ولا في السماء وهو السميع العليم.",
            "سنن أبي داود 5088 — صححه الألباني", 3)
        Text("أذكار النوم والاستيقاظ", color = Navy, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        DhikrTextCard(
            title = "عند النوم",
            phrase = "باسمك اللهم أموت وأحيا",
            source = "صحيح البخاري، حديث 6324"
        )
        DhikrTextCard(
            title = "عند الاستيقاظ",
            phrase = "الحمد لله الذي أحيانا بعدما أماتنا وإليه النشور",
            source = "صحيح البخاري، حديث 6312"
        )
        Text("الأعداد المذكورة هنا واردة في الحديث، والعدّاد يحفظ تقدمك على هذا الجهاز.", color = Ink.copy(alpha = .7f), fontSize = 12.sp)
        Text("العدّاد أداة مساعدة. الأعداد الواردة في الأذكار المأثورة مذكورة في مواضعها مع مصادرها.", color = Ink.copy(alpha = .7f), fontSize = 12.sp)
    }
}

@Composable
private fun DhikrTextCard(title: String, phrase: String, source: String) {
    Surface(color = Color.White, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text(title, color = Navy, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(phrase, color = Ink, fontSize = 20.sp, lineHeight = 32.sp)
            Text(source, color = Gold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun DhikrProgressCard(
    prefs: android.content.SharedPreferences,
    storageKey: String,
    phrase: String,
    source: String,
    target: Int
) {
    var count by remember { mutableIntStateOf(prefs.getInt(storageKey, 0).coerceIn(0, target)) }
    Surface(color = Color.White, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text(phrase, color = Navy, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Text(source, color = Gold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("$count / $target", color = Navy, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Button(
                    onClick = {
                        count = (count + 1).coerceAtMost(target)
                        prefs.edit().putInt(storageKey, count).apply()
                    },
                    enabled = count < target,
                    colors = ButtonDefaults.buttonColors(containerColor = Navy)
                ) { Text(if (count == target) "اكتمل" else "تسبيح") }
            }
            LinearProgressIndicator(progress = { count.toFloat() / target.toFloat() }, modifier = Modifier.fillMaxWidth(), color = Gold)
            TextButton(onClick = { count = 0; prefs.edit().putInt(storageKey, 0).apply() }) { Text("تصفير") }
        }
    }
}

@Composable
private fun DhikrCounterCard(
    prefs: android.content.SharedPreferences,
    storageKey: String,
    title: String,
    morningText: String,
    eveningText: String,
    source: String,
    target: Int
) {
    var isEvening by remember { mutableStateOf(false) }
    var count by remember { mutableIntStateOf(prefs.getInt(storageKey, 0).coerceIn(0, target)) }
    Surface(color = Color.White, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, color = Navy, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = !isEvening, onClick = { isEvening = false }, label = { Text("الصباح") })
                FilterChip(selected = isEvening, onClick = { isEvening = true }, label = { Text("المساء") })
            }
            Text(if (isEvening) eveningText else morningText, color = Ink, fontSize = 19.sp, lineHeight = 32.sp)
            Text(source, color = Gold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("$count / $target", color = Navy, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Button(onClick = {
                    count = (count + 1).coerceAtMost(target)
                    prefs.edit().putInt(storageKey, count).apply()
                }, enabled = count < target, colors = ButtonDefaults.buttonColors(containerColor = Navy)) {
                    Text(if (count >= target) "اكتمل" else "تم الذكر")
                }
            }
            LinearProgressIndicator(progress = { count.toFloat() / target.toFloat() }, modifier = Modifier.fillMaxWidth(), color = Gold)
            TextButton(onClick = { count = 0; prefs.edit().putInt(storageKey, 0).apply() }) { Text("إعادة العداد") }
        }
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
