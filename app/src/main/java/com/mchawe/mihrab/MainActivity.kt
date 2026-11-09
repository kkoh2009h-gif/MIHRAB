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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        Text("مِحراب • الإصدار 1.0.5", color = Ink.copy(alpha = .45f), fontSize = 11.sp)
    }
}

@Composable
private fun PrayerScreen(modifier: Modifier = Modifier) {
    val cities = listOf("كركوك", "بغداد", "الموصل", "أربيل", "السليمانية", "دهوك", "البصرة", "النجف", "كربلاء", "الأنبار")
    val prayerNames = listOf("الفجر", "الشروق", "الظهر", "العصر", "المغرب", "العشاء")
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mihrab", Context.MODE_PRIVATE) }
    var city by remember { mutableStateOf(prefs.getString("city", "كركوك") ?: "كركوك") }
    val dateKey = java.time.LocalDate.now().toString()
    val storageKey = "official_manual_${city}_${dateKey}"
    var times by remember(city, dateKey) {
        mutableStateOf(prayerNames.mapIndexed { index, _ -> prefs.getString("${storageKey}_${index}", "") ?: "" })
    }
    var expanded by remember { mutableStateOf(false) }
    var showEntry by remember { mutableStateOf(false) }
    var editTimes by remember { mutableStateOf(times) }
    val validEntries = editTimes.all { it.matches(Regex("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) }

    if (showEntry) {
        AlertDialog(
            onDismissRequest = { showEntry = false },
            title = { Text("إدخال مواقيت اليوم") },
            text = {
                Column(
                    modifier = Modifier.heightIn(max = 430.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("انقل الأوقات كما تظهر في جدول الديوان الرسمي لمدينة $city بتاريخ $dateKey. لا تدخل أوقاتاً تقديرية.", color = Ink.copy(alpha = .75f), fontSize = 12.sp)
                    prayerNames.forEachIndexed { index, prayer ->
                        OutlinedTextField(
                            value = editTimes[index],
                            onValueChange = { value -> editTimes = editTimes.toMutableList().also { it[index] = value } },
                            label = { Text(prayer) },
                            placeholder = { Text("مثال: 05:10") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = editTimes[index].isNotBlank() && !editTimes[index].matches(Regex("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Text("اكتب الوقت بصيغة 05:10 أو 17:45. يجب إدخال الأوقات الستة بصيغة صحيحة.", color = if (validEntries) Ink.copy(alpha = .6f) else MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(enabled = validEntries, onClick = {
                    times = editTimes.map { it.trim().padStart(5, '0') }
                    val editor = prefs.edit()
                    times.forEachIndexed { index, value -> editor.putString("${storageKey}_${index}", value.trim()) }
                    editor.apply()
                    showEntry = false
                }) { Text("حفظ") }
            },
            dismissButton = { TextButton(onClick = { showEntry = false }) { Text("إلغاء") } }
        )
    }

    PageColumn(modifier) {
        PageHeader("مواقيت الصلاة", "مواقيت اليوم حسب المدينة المحددة")
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
                                prefs.edit().putString("city", item).apply()
                                expanded = false
                            })
                        }
                    }
                }
                Text("المصدر الرسمي للمراجعة: ديوان الوقف السني العراقي.", color = Color.White.copy(alpha = .85f), fontSize = 12.sp)
                OutlinedButton(onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://dws.gov.iq/")))
                }) {
                    Icon(Icons.Default.OpenInNew, null)
                    Spacer(Modifier.width(8.dp))
                    Text("فتح موقع الديوان")
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("جدول اليوم", color = Navy, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(dateKey, color = Ink.copy(alpha = .6f), fontSize = 11.sp)
            }
            Button(onClick = { editTimes = times; showEntry = true }, colors = ButtonDefaults.buttonColors(containerColor = Navy)) {
                Icon(Icons.Default.Edit, null)
                Spacer(Modifier.width(5.dp))
                Text("إدخال الأوقات")
            }
        }
        prayerNames.forEachIndexed { index, prayer ->
            Surface(color = Color.White, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(prayer, color = Navy, fontWeight = FontWeight.SemiBold)
                    Text(times[index].ifBlank { "غير مدخل" }, color = if (times[index].isBlank()) Ink.copy(alpha = .45f) else Navy, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Text(
            if (times.any { it.isNotBlank() }) "هذه الأوقات أدخلها المستخدم يدوياً؛ راجعها مع جدول الديوان قبل الاعتماد عليها. تُحفظ لهذا اليوم ولهذه المدينة فقط."
            else "لم تُدخل مواقيت هذا اليوم بعد. لا يعرض التطبيق أوقاتاً تقديرية أو من مصدر غير رسمي على أنها مواقيت الديوان.",
            color = Ink.copy(alpha = .7f), fontSize = 12.sp
        )
    }
}


private data class QuranAyah(val number: Int, val text: String)
private data class QuranSurah(val number: Int, val ayat: List<QuranAyah>)

private val arabicSurahNames = listOf(
"الفاتحة","البقرة","آل عمران","النساء","المائدة","الأنعام","الأعراف","الأنفال","التوبة","يونس","هود","يوسف","الرعد","إبراهيم","الحجر","النحل","الإسراء","الكهف","مريم","طه","الأنبياء","الحج","المؤمنون","النور","الفرقان","الشعراء","النمل","القصص","العنكبوت","الروم","لقمان","السجدة","الأحزاب","سبأ","فاطر","يس","الصافات","ص","الزمر","غافر","فصلت","الشورى","الزخرف","الدخان","الجاثية","الأحقاف","محمد","الفتح","الحجرات","ق","الذاريات","الطور","النجم","القمر","الرحمن","الواقعة","الحديد","المجادلة","الحشر","الممتحنة","الصف","الجمعة","المنافقون","التغابن","الطلاق","التحريم","الملك","القلم","الحاقة","المعارج","نوح","الجن","المزمل","المدثر","القيامة","الإنسان","المرسلات","النبأ","النازعات","عبس","التكوير","الانفطار","المطففين","الانشقاق","البروج","الطارق","الأعلى","الغاشية","الفجر","البلد","الشمس","الليل","الضحى","الشرح","التين","العلق","القدر","البينة","الزلزلة","العاديات","القارعة","التكاثر","العصر","الهمزة","الفيل","قريش","الماعون","الكوثر","الكافرون","النصر","المسد","الإخلاص","الفلق","الناس"
)

@Composable
private fun QuranScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mihrab", Context.MODE_PRIVATE) }
    var targetDays by remember { mutableIntStateOf(prefs.getInt("khatmah_days", 30)) }
    var completedPages by remember { mutableIntStateOf(prefs.getInt("khatmah_pages", 0).coerceIn(0, 604)) }
    var currentPage by remember { mutableIntStateOf(prefs.getInt("quran_current_page", 1).coerceIn(1, 604)) }
    var savedPage by remember { mutableIntStateOf(prefs.getInt("quran_saved_page", 1).coerceIn(1, 604)) }
    var showJumpDialog by remember { mutableStateOf(false) }
    var jumpPageText by remember { mutableStateOf(currentPage.toString()) }
    var surahIndex by remember { mutableIntStateOf(prefs.getInt("quran_surah", 1).coerceIn(1, 114)) }
    var surahs by remember { mutableStateOf<List<QuranSurah>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf(false) }
    var surahMenu by remember { mutableStateOf(false) }
    val dailyPages = kotlin.math.ceil(604.0 / targetDays).toInt()
    val progress = completedPages / 604f

    LaunchedEffect(currentPage) {
        val pageSurah = quranPageStarts[currentPage - 1].first
        if (surahIndex != pageSurah) {
            surahIndex = pageSurah
            prefs.edit().putInt("quran_surah", pageSurah).apply()
        }
    }

    LaunchedEffect(Unit) {
        try {
            val loaded = withContext(Dispatchers.IO) {
                context.assets.open("quran-uthmani.txt").bufferedReader(Charsets.UTF_8).useLines { lines ->
                    lines.mapNotNull { line ->
                        val parts = line.split('|', limit = 3)
                        if (parts.size != 3) null else {
                            val s = parts[0].toIntOrNull()
                            val a = parts[1].toIntOrNull()
                            if (s == null || a == null || s !in 1..114 || a < 1 || parts[2].isBlank()) null
                            else Triple(s, a, parts[2])
                        }
                    }.toList().groupBy { it.first }.map { (number, verses) ->
                        QuranSurah(number, verses.map { QuranAyah(it.second, it.third) }.sortedBy { it.number })
                    }.sortedBy { it.number }
                }
            }
            surahs = loaded
            loadError = loaded.size != 114 || loaded.sumOf { it.ayat.size } != 6236
        } catch (_: Exception) {
            loadError = true
        } finally {
            loading = false
        }
    }

    if (showJumpDialog) {
        val requestedPage = jumpPageText.toIntOrNull()
        AlertDialog(
            onDismissRequest = { showJumpDialog = false },
            title = { Text("الانتقال إلى صفحة") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = jumpPageText,
                        onValueChange = { jumpPageText = it.filter(Char::isDigit).take(3) },
                        label = { Text("رقم الصفحة") },
                        placeholder = { Text("1 - 604") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = requestedPage == null || requestedPage !in 1..604
                    )
                }
            },
            confirmButton = {
                TextButton(enabled = requestedPage != null && requestedPage in 1..604, onClick = {
                    currentPage = requestedPage!!
                    prefs.edit().putInt("quran_current_page", currentPage).apply()
                    showJumpDialog = false
                }) { Text("انتقال") }
            },
            dismissButton = { TextButton(onClick = { showJumpDialog = false }) { Text("إلغاء") } }
        )
    }

    PageColumn(modifier) {
        PageHeader("القرآن الكريم", "الرسم العثماني — رواية حفص")
        Surface(color = Color.White, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("نص المصحف من مشروع تنزيل Tanzil، الإصدار 1.1، دون تغيير في النص. يُضمّن داخل التطبيق ليعمل دون إنترنت.", color = Ink.copy(alpha = .75f), fontSize = 13.sp, lineHeight = 21.sp)
                TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://tanzil.net/"))) }) {
                    Text("المصدر وشروط الاستخدام: Tanzil")
                }
            }
        }
        Text("اختر السورة", color = Navy, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Box {
            OutlinedButton(onClick = { surahMenu = true }, modifier = Modifier.fillMaxWidth()) {
                Text(arabicSurahNames[surahIndex - 1] + " • السورة " + surahIndex)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.ArrowDropDown, null)
            }
            DropdownMenu(expanded = surahMenu, onDismissRequest = { surahMenu = false }) {
                arabicSurahNames.forEachIndexed { index, name ->
                    DropdownMenuItem(text = { Text("${index + 1}. " + name) }, onClick = {
                        surahIndex = index + 1
                        val pageForSurah = quranPageStarts.indexOfLast { it.first < surahIndex || (it.first == surahIndex && it.second <= 1) }
                            .coerceAtLeast(0) + 1
                        currentPage = pageForSurah
                        prefs.edit().putInt("quran_surah", surahIndex).putInt("quran_current_page", currentPage).apply()
                        surahMenu = false
                    })
                }
            }
        }
        when {
            loading -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CircularProgressIndicator(color = Gold, modifier = Modifier.size(24.dp))
                Text("جارٍ تجهيز المصحف...", color = Ink.copy(alpha = .7f))
            }
            loadError -> Surface(color = Color.White, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Text("تعذّر التحقق من اكتمال ملف المصحف؛ أوقفنا عرض النص احتياطاً.", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            }
            else -> {
                val allAyahs = surahs.flatMap { surah ->
                    surah.ayat.map { ayah -> Triple(surah.number, ayah.number, ayah.text) }
                }
                val pageStart = quranPageStarts[currentPage - 1]
                val nextPageStart = quranPageStarts.getOrNull(currentPage)
                val startIndex = allAyahs.indexOfFirst { it.first == pageStart.first && it.second == pageStart.second }
                val endIndex = if (nextPageStart == null) allAyahs.size else allAyahs.indexOfFirst {
                    it.first == nextPageStart.first && it.second == nextPageStart.second
                }.let { if (it < 0) allAyahs.size else it }
                val pageAyahs = if (startIndex >= 0 && endIndex >= startIndex) allAyahs.subList(startIndex, endIndex) else emptyList()
                if (pageAyahs.isNotEmpty()) {
                    Surface(
                        color = Navy,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("مُصْحَفُ الْمَدِينَةِ الْمُنَوَّرَة", color = Gold, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            Text("الجزء ${((currentPage - 1) / 20) + 1}  •  الصفحة $currentPage", color = Color.White.copy(alpha = .85f), fontSize = 12.sp)
                        }
                    }
                    Surface(
                        color = Color(0xFFFFFDF5),
                        shape = RoundedCornerShape(5.dp),
                        border = BorderStroke(1.dp, Gold.copy(alpha = .75f)),
                        modifier = Modifier.fillMaxWidth()
                            .pointerInput(currentPage) {
                                var drag = 0f
                                detectHorizontalDragGestures(
                                    onHorizontalDrag = { _, amount -> drag += amount },
                                    onDragEnd = {
                                        if (drag < -45f && currentPage < 604) {
                                            currentPage += 1
                                            prefs.edit().putInt("quran_current_page", currentPage).apply()
                                        } else if (drag > 45f && currentPage > 1) {
                                            currentPage -= 1
                                            prefs.edit().putInt("quran_current_page", currentPage).apply()
                                        }
                                        drag = 0f
                                    }
                                )
                            }
                    ) {
                        Column(
                            Modifier.fillMaxWidth().padding(horizontal = 17.dp, vertical = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("۞  الْمُصْحَفُ الشَّرِيف  ۞", color = Gold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Divider(color = Gold.copy(alpha = .65f))
                            pageAyahs.forEachIndexed { index, item ->
                                val (surahNumber, ayahNumber, ayahText) = item
                                if (index == 0 || pageAyahs[index - 1].first != surahNumber) {
                                    Spacer(Modifier.height(4.dp))
                                    Text("سُورَةُ " + arabicSurahNames[surahNumber - 1], color = Navy, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    ayahText + "  ﴿" + ayahNumber + "﴾",
                                    color = Color(0xFF33291F), fontSize = 22.sp, lineHeight = 42.sp,
                                    textAlign = TextAlign.Justify, modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Divider(color = Gold.copy(alpha = .65f))
                            Text("—  $currentPage  —", color = Navy, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("اسحب أفقياً للتنقل بين الصفحات", color = Ink.copy(alpha = .55f), fontSize = 10.sp)
                        }
                    }
                    Text("توزيع الآيات مرتبط بفهرس صفحات مصحف المدينة ذي 604 صفحات.", color = Ink.copy(alpha = .7f), fontSize = 11.sp)
                } else {
                    Text("تعذّر تحديد آيات هذه الصفحة من الفهرس.", color = MaterialTheme.colorScheme.error)
                }

                }
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
                Text("الصفحة الحالية: $currentPage / 604", color = Navy, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                OutlinedButton(onClick = { jumpPageText = currentPage.toString(); showJumpDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.FindInPage, null); Spacer(Modifier.width(8.dp)); Text("الانتقال إلى رقم صفحة")
                }
                LinearProgressIndicator(progress = { currentPage / 604f }, modifier = Modifier.fillMaxWidth(), color = Gold)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedButton(onClick = { currentPage = (currentPage - 1).coerceAtLeast(1); prefs.edit().putInt("quran_current_page", currentPage).apply() }, enabled = currentPage > 1) { Text("السابقة") }
                    Button(onClick = { currentPage = (currentPage + 1).coerceAtMost(604); prefs.edit().putInt("quran_current_page", currentPage).apply() }, enabled = currentPage < 604, colors = ButtonDefaults.buttonColors(containerColor = Navy)) { Text("التالية") }
                }
                Text("آخر موضع محفوظ: صفحة $savedPage", color = Navy, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { savedPage = currentPage; prefs.edit().putInt("quran_saved_page", savedPage).apply() }, colors = ButtonDefaults.buttonColors(containerColor = Navy)) { Text("حفظ الموضع") }
                    OutlinedButton(onClick = { currentPage = savedPage; prefs.edit().putInt("quran_current_page", currentPage).apply() }) { Text("استرجاع المحفوظ") }
                }
                Text("ربط أرقام صفحات المصحف المطبوعة بالسور قيد الإضافة.", color = Ink.copy(alpha = .6f), fontSize = 12.sp)
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
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mihrab", Context.MODE_PRIVATE) }
    var adhanMaster by remember { mutableStateOf(prefs.getBoolean("adhan_master", false)) }
    var vibration by remember { mutableStateOf(prefs.getBoolean("adhan_vibration", true)) }
    val prayerNames = listOf("الفجر", "الظهر", "العصر", "المغرب", "العشاء")
    val prayerKeys = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
    val enabled = prayerKeys.map { key ->
        remember { mutableStateOf(prefs.getBoolean("adhan_$key", true)) }
    }
    PageColumn(modifier) {
        PageHeader("المزيد", "إعدادات وأدوات مِحراب")
        Text("اختيار صوت الأذان", color = Navy, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        val soundOptions = listOf("الصوت الافتراضي", "أذان مكة", "أذان المدينة", "أذان هادئ")
        var selectedSound by remember { mutableIntStateOf(prefs.getInt("adhan_sound", 0).coerceIn(0, soundOptions.lastIndex)) }
        var soundMenu by remember { mutableStateOf(false) }
        Surface(color = Color.White, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("الصوت المختار", color = Ink.copy(alpha = .65f), fontSize = 12.sp)
                Box {
                    OutlinedButton(onClick = { soundMenu = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(soundOptions[selectedSound])
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                    DropdownMenu(expanded = soundMenu, onDismissRequest = { soundMenu = false }) {
                        soundOptions.forEachIndexed { index, option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = {
                                selectedSound = index
                                prefs.edit().putInt("adhan_sound", index).apply()
                                soundMenu = false
                            })
                        }
                    }
                }
                Text("اختيار الصوت يُحفظ كتفضيل فقط. ملفات الأذان والمعاينة والتشغيل بالخلفية ستُضاف بعد توفير ملفات مرخّصة واختبار خدمة التشغيل.", color = Ink.copy(alpha = .65f), fontSize = 12.sp, lineHeight = 19.sp)
            }
        }
        Text("إعدادات الأذان", color = Navy, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Surface(color = Color.White, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("تفعيل تنبيهات الأذان", color = Navy, fontWeight = FontWeight.Bold)
                        Text("مفتاح عام لإعدادات التنبيه", color = Ink.copy(alpha = .6f), fontSize = 12.sp)
                    }
                    Switch(checked = adhanMaster, onCheckedChange = {
                        adhanMaster = it
                        prefs.edit().putBoolean("adhan_master", it).apply()
                    })
                }
                Divider(color = Cream)
                prayerNames.forEachIndexed { index, name ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(name, color = Navy, fontWeight = FontWeight.SemiBold)
                        Switch(checked = enabled[index].value, onCheckedChange = {
                            enabled[index].value = it
                            prefs.edit().putBoolean("adhan_${prayerKeys[index]}", it).apply()
                        })
                    }
                }
                Divider(color = Cream)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("الاهتزاز", color = Navy, fontWeight = FontWeight.SemiBold)
                        Text("تفضيل محفوظ محلياً", color = Ink.copy(alpha = .6f), fontSize = 12.sp)
                    }
                    Switch(checked = vibration, onCheckedChange = {
                        vibration = it
                        prefs.edit().putBoolean("adhan_vibration", it).apply()
                    })
                }
            }
        }
        Surface(color = Gold.copy(alpha = .14f), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Text(
                "تنبيه مهم: هذه إعدادات محفوظة على جهازك فقط. تشغيل الأذان والتنبيهات بالخلفية غير مفعّل حالياً؛ لن نصدر تنبيهاً اعتماداً على أوقات غير موثقة.",
                color = Navy, fontSize = 12.sp, lineHeight = 20.sp, modifier = Modifier.padding(14.dp)
            )
        }
        FeatureTile("التقويم الهجري", "التاريخ الهجري والمناسبات الثابتة", Icons.Default.CalendarMonth)
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
