package com.mchawe.mihrab

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.app.Activity
import android.graphics.BitmapFactory
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.activity.compose.setContent
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
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

private var Navy by mutableStateOf(Color(0xFF101F35))
private var Gold by mutableStateOf(Color(0xFFC8A96A))
private var Cream by mutableStateOf(Color(0xFFF6F2E9))
private var Ink by mutableStateOf(Color(0xFF1C2A3A))

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MihrabApp() }
    }
}

@Composable
private fun MihrabApp() {
    var selected by remember { mutableIntStateOf(0) }
    var quranFullscreen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val appPrefs = remember { context.getSharedPreferences("mihrab", Context.MODE_PRIVATE) }
    var darkMode by remember { mutableStateOf(appPrefs.getBoolean("dark_mode", false)) }
    LaunchedEffect(darkMode) {
        Navy = if (darkMode) Color(0xFFE8EAF0) else Color(0xFF101F35)
        Gold = Color(0xFFC8A96A)
        Cream = if (darkMode) Color(0xFF10141C) else Color(0xFFF6F2E9)
        Ink = if (darkMode) Color(0xFFE1E5EC) else Color(0xFF1C2A3A)
    }
    val labels = listOf("الرئيسية", "الصلاة", "القرآن", "الأذكار", "المزيد")
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.AccessTime,
        Icons.Default.MenuBook,
        Icons.Default.Favorite,
        Icons.Default.MoreHoriz
    )

    MaterialTheme(colorScheme = if (darkMode) darkColorScheme(
        primary = Gold, secondary = Gold, background = Cream, surface = Color(0xFF202733),
        onBackground = Ink, onSurface = Ink
    ) else lightColorScheme(
        primary = Navy, secondary = Gold, background = Cream, surface = Color.White
    )) {
        Scaffold(
            containerColor = Cream,
            bottomBar = {
                if (!quranFullscreen) NavigationBar(containerColor = if (darkMode) Color(0xFF202733) else Color.White) {
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
                2 -> QuranScreen(modifier = if (quranFullscreen) Modifier else Modifier.padding(padding), isFullscreen = quranFullscreen, onFullscreenChange = { quranFullscreen = it })
                3 -> AdhkarScreen(Modifier.padding(padding))
                else -> MoreScreen(Modifier.padding(padding), darkMode = darkMode, onDarkModeChange = { darkMode = it; appPrefs.edit().putBoolean("dark_mode", it).apply() })
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
    val cities = IraqiPrayerCalculator.cities
    val prayerNames = listOf("الفجر", "الشروق", "الظهر", "العصر", "المغرب", "العشاء")
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mihrab", Context.MODE_PRIVATE) }
    var city by remember { mutableStateOf(prefs.getString("city", "كركوك") ?: "كركوك") }
    var selectedZoneId by remember { mutableStateOf(prefs.getString("selected_zone", java.time.ZoneId.systemDefault().id) ?: java.time.ZoneId.systemDefault().id) }
    var showGlobalCityPicker by remember { mutableStateOf(false) }
    var globalCityQuery by remember { mutableStateOf("") }
    var globalCityResults by remember { mutableStateOf<List<GlobalCityCatalog.City>>(emptyList()) }
    var selectedMethod by remember { mutableStateOf(GlobalPrayerCalculator.Method.valueOf(prefs.getString("global_method", GlobalPrayerCalculator.Method.MUSLIM_WORLD_LEAGUE.name) ?: GlobalPrayerCalculator.Method.MUSLIM_WORLD_LEAGUE.name)) }
    var selectedAsr by remember { mutableStateOf(GlobalPrayerCalculator.AsrMethod.valueOf(prefs.getString("asr_method", GlobalPrayerCalculator.AsrMethod.SHAFII.name) ?: GlobalPrayerCalculator.AsrMethod.SHAFII.name)) }
    var methodExpanded by remember { mutableStateOf(false) }
    var asrExpanded by remember { mutableStateOf(false) }
    var locationMessage by remember { mutableStateOf("لم يتم تحديد الموقع بعد") }
    var locating by remember { mutableStateOf(false) }
    var adhanEnabled by remember { mutableStateOf(prefs.getBoolean("global_adhan_enabled", false)) }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted || Build.VERSION.SDK_INT < 33) {
            prefs.edit().putBoolean("global_adhan_enabled", true).apply()
            adhanEnabled = true
            PrayerAlarmScheduler.schedule(context)
        } else locationMessage = "يجب السماح بالإشعارات حتى تظهر تنبيهات الصلاة."
    }
    val date = java.time.LocalDate.now()
    val dateKey = date.toString()
    val storageKey = "official_manual_${city}_${dateKey}"
    var manualTimes by remember(city, dateKey) {
        mutableStateOf(prayerNames.mapIndexed { index, _ -> prefs.getString("${storageKey}_${index}", "") ?: "" })
    }
    val savedLatitude = prefs.getFloat("last_latitude", Float.NaN).toDouble()
    val savedLongitude = prefs.getFloat("last_longitude", Float.NaN).toDouble()
    val selectedCity = cities.firstOrNull { it.name == city }
    val latitude = if (savedLatitude.isFinite()) savedLatitude else selectedCity?.latitude ?: 35.4681
    val longitude = if (savedLongitude.isFinite()) savedLongitude else selectedCity?.longitude ?: 44.3922
    val calculated = remember(latitude, longitude, dateKey, selectedZoneId, selectedMethod, selectedAsr) {
        GlobalPrayerCalculator.calculate(latitude, longitude, date, selectedZoneId, selectedMethod, selectedAsr)
    }
    LaunchedEffect(showGlobalCityPicker, globalCityQuery) {
        if (showGlobalCityPicker && globalCityQuery.trim().length >= 2) {
            globalCityResults = withContext(Dispatchers.IO) { GlobalCityCatalog.search(context, globalCityQuery, 80) }
        } else globalCityResults = emptyList()
    }
    val calculatedList = calculated?.let { listOf(it.fajr, it.sunrise, it.dhuhr, it.asr, it.maghrib, it.isha) }
    val displayedTimes = manualTimes.mapIndexed { index, value -> value.ifBlank { calculatedList?.get(index) ?: "" } }
    LaunchedEffect(adhanEnabled, latitude, longitude, selectedZoneId, selectedMethod, selectedAsr) {
        if (adhanEnabled) PrayerAlarmScheduler.schedule(context)
    }
    var expanded by remember { mutableStateOf(false) }
    var showEntry by remember { mutableStateOf(false) }
    var editTimes by remember { mutableStateOf(manualTimes) }

    fun applyDetectedLocation(location: Location) {
        val nearest = cities.minByOrNull { item ->
            val result = FloatArray(1)
            Location.distanceBetween(location.latitude, location.longitude, item.latitude, item.longitude, result)
            result[0]
        }
        if (nearest != null) {
            val distance = FloatArray(1)
            Location.distanceBetween(location.latitude, location.longitude, nearest.latitude, nearest.longitude, distance)
            val placeLabel = if (distance[0] <= 50000f) nearest.name else "موقعك الحالي"
            city = placeLabel
            selectedZoneId = java.time.ZoneId.systemDefault().id
            prefs.edit().putString("city", placeLabel)
                .putString("selected_zone", selectedZoneId)
                .putFloat("last_latitude", location.latitude.toFloat())
                .putFloat("last_longitude", location.longitude.toFloat()).apply()
            locationMessage = "تم تحديد إحداثيات موقعك بدقة واستخدامها مباشرة في حساب المواقيت. المنطقة الزمنية: ${java.time.ZoneId.systemDefault().id}"
        }
        locating = false
    }

    fun detectLocation() {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
            val last = providers.asSequence().filter { manager.isProviderEnabled(it) }
                .mapNotNull { provider -> try { manager.getLastKnownLocation(provider) } catch (_: SecurityException) { null } }
                .maxByOrNull { it.time }
            if (last != null) {
                applyDetectedLocation(last)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val provider = providers.firstOrNull { manager.isProviderEnabled(it) }
                if (provider == null) {
                    locating = false
                    locationMessage = "فعّل خدمة الموقع GPS من إعدادات الهاتف ثم حاول مجدداً"
                } else {
                    manager.getCurrentLocation(provider, null, context.mainExecutor) { result ->
                        if (result != null) applyDetectedLocation(result)
                        else {
                            locating = false
                            locationMessage = "تعذر تحديد الموقع. تأكد من تشغيل GPS وحاول مجدداً."
                        }
                    }
                }
            } else {
                locating = false
                locationMessage = "لم يتوفر موقع حديث. فعّل GPS وحاول مجدداً."
            }
        } catch (_: SecurityException) {
            locating = false
            locationMessage = "لم تُمنح صلاحية الموقع. يمكنك اختيار المحافظة يدوياً."
        } catch (_: Exception) {
            locating = false
            locationMessage = "تعذر الوصول للموقع حالياً. اختر المحافظة يدوياً."
        }
    }

    val locationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            locating = true
            locationMessage = "جارٍ تحديد موقعك..."
            detectLocation()
        } else {
            locationMessage = "رفضت صلاحية الموقع؛ اختيار المحافظة يدوياً متاح دائماً."
        }
    }

    if (showGlobalCityPicker) {
        AlertDialog(
            onDismissRequest = { showGlobalCityPicker = false },
            title = { Text("اختيار مدينة من العالم") },
            text = {
                Column(Modifier.heightIn(max = 520.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("ابحث باسم المدينة بالإنجليزية أو باسم الدولة. قاعدة المدن محفوظة داخل التطبيق وتعمل دون إنترنت.", color = Ink.copy(alpha = .7f), fontSize = 12.sp)
                    OutlinedTextField(
                        value = globalCityQuery,
                        onValueChange = { globalCityQuery = it },
                        label = { Text("اسم المدينة أو الدولة") },
                        placeholder = { Text("London, Istanbul, Tokyo...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (globalCityQuery.trim().length < 2) {
                        Text("اكتب حرفين على الأقل لعرض النتائج.", color = Ink.copy(alpha = .6f), fontSize = 12.sp)
                    } else if (globalCityResults.isEmpty()) {
                        Text("لا توجد نتائج مطابقة. جرّب كتابة الاسم بالإنجليزية.", color = Ink.copy(alpha = .6f), fontSize = 12.sp)
                    } else {
                        Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            globalCityResults.forEach { result ->
                                TextButton(
                                    onClick = {
                                        city = result.label
                                        selectedZoneId = result.timeZone
                                        prefs.edit().putString("city", result.label)
                                            .putString("selected_zone", result.timeZone)
                                            .putFloat("last_latitude", result.latitude.toFloat())
                                            .putFloat("last_longitude", result.longitude.toFloat()).apply()
                                        locationMessage = "تم اختيار المدينة يدوياً من قاعدة GeoNames العالمية. المنطقة الزمنية: ${result.timeZone}"
                                        showGlobalCityPicker = false
                                        globalCityQuery = ""
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                                        Text(result.name, color = Navy, fontWeight = FontWeight.SemiBold)
                                        Text(result.country + " • " + result.timeZone, color = Ink.copy(alpha = .6f), fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showGlobalCityPicker = false }) { Text("إغلاق") } }
        )
    }

    if (showEntry) {
        AlertDialog(
            onDismissRequest = { showEntry = false },
            title = { Text("إدخال مواقيت اليوم") },
            text = {
                Column(Modifier.heightIn(max = 430.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("الأوقات اليدوية تتقدم على الحساب الفلكي التقريبي لمدينة $city بتاريخ $dateKey.", color = Ink.copy(alpha = .75f), fontSize = 12.sp)
                    prayerNames.forEachIndexed { index, prayer ->
                        OutlinedTextField(
                            value = editTimes[index],
                            onValueChange = { value -> editTimes = editTimes.toMutableList().also { it[index] = value } },
                            label = { Text(prayer) }, placeholder = { Text("مثال: 05:10") }, singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = editTimes[index].isNotBlank() && !editTimes[index].matches(Regex("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Text("اترك الخانة فارغة لاستخدام الحساب الفلكي التقريبي.", color = Ink.copy(alpha = .6f), fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(enabled = editTimes.all { it.isBlank() || it.matches(Regex("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) }, onClick = {
                    manualTimes = editTimes.map { it.trim().let { value -> if (value.isBlank()) "" else value.padStart(5, '0') } }
                    val editor = prefs.edit()
                    manualTimes.forEachIndexed { index, value -> editor.putString("${storageKey}_${index}", value) }
                    editor.apply()
                    showEntry = false
                }) { Text("حفظ") }
            },
            dismissButton = { TextButton(onClick = { showEntry = false }) { Text("إلغاء") } }
        )
    }

    PageColumn(modifier) {
        PageHeader("مواقيت الصلاة", "تحديد تلقائي للموقع أو اختيار المحافظة يدوياً")
        Surface(color = Navy, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("موقعك الحالي", color = Gold, fontWeight = FontWeight.Bold)
                Text(city, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                Text(locationMessage, color = Color.White.copy(alpha = .82f), fontSize = 12.sp, lineHeight = 18.sp)
                Button(
                    onClick = {
                        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        if (fine || coarse) {
                            locating = true
                            locationMessage = "جارٍ تحديد موقعك..."
                            detectLocation()
                        } else locationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    },
                    enabled = !locating,
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (locating) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Icon(Icons.Default.MyLocation, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (locating) "جارٍ تحديد الموقع..." else "تحديد موقعي تلقائياً")
                }
                Box {
                    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.LocationCity, null); Spacer(Modifier.width(8.dp)); Text("اختيار المحافظة يدوياً")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        cities.forEach { item ->
                            DropdownMenuItem(text = { Text(item.name) }, onClick = {
                                city = item.name
                                selectedZoneId = "Asia/Baghdad"
                                prefs.edit().remove("last_latitude").remove("last_longitude")
                                    .putString("selected_zone", selectedZoneId).putString("city", item.name).apply()
                                locationMessage = "تم اختيار المحافظة يدوياً"
                                expanded = false
                            })
                        }
                    }
                }
                OutlinedButton(onClick = { showGlobalCityPicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Public, null); Spacer(Modifier.width(8.dp)); Text("اختيار مدينة من جميع دول العالم")
                }
                Text("التحديد التلقائي يستخدم إحداثيات GPS والمنطقة الزمنية للهاتف، والاختيار اليدوي يستخدم قاعدة مدن عالمية تعمل دون إنترنت. لا تُرسل إحداثياتك إلى أي خادم.", color = Color.White.copy(alpha = .78f), fontSize = 11.sp)
                Box {
                    OutlinedButton(onClick = { methodExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("طريقة الحساب: ${selectedMethod.title}")
                    }
                    DropdownMenu(expanded = methodExpanded, onDismissRequest = { methodExpanded = false }) {
                        GlobalPrayerCalculator.Method.values().forEach { item ->
                            DropdownMenuItem(text = { Text(item.title) }, onClick = {
                                selectedMethod = item
                                prefs.edit().putString("global_method", item.name).apply()
                                methodExpanded = false
                            })
                        }
                    }
                }
                Box {
                    OutlinedButton(onClick = { asrExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("حساب العصر: ${selectedAsr.title}")
                    }
                    DropdownMenu(expanded = asrExpanded, onDismissRequest = { asrExpanded = false }) {
                        GlobalPrayerCalculator.AsrMethod.values().forEach { item ->
                            DropdownMenuItem(text = { Text(item.title) }, onClick = {
                                selectedAsr = item
                                prefs.edit().putString("asr_method", item.name).apply()
                                asrExpanded = false
                            })
                        }
                    }
                }
            }
        }
        Surface(color = Color.White, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("تنبيهات الأذان", color = Navy, fontWeight = FontWeight.Bold)
                        Text("تنبيه عند دخول أوقات الصلوات الخمس", color = Ink.copy(alpha = .65f), fontSize = 11.sp)
                    }
                    Switch(checked = adhanEnabled, onCheckedChange = { enabled ->
                        if (enabled) {
                            if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                prefs.edit().putBoolean("global_adhan_enabled", true).apply()
                                adhanEnabled = true
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !(context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager).canScheduleExactAlarms()) {
                                    context.startActivity(Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply { data = Uri.parse("package:" + context.packageName); addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                                } else PrayerAlarmScheduler.schedule(context)
                            }
                        } else {
                            prefs.edit().putBoolean("global_adhan_enabled", false).apply()
                            adhanEnabled = false
                            PrayerAlarmScheduler.cancelAll(context)
                        }
                    })
                }
                Text("يستخدم صوت التنبيه الافتراضي للهاتف حالياً. قد يحتاج Android إلى السماح بالإشعارات والمنبهات الدقيقة من الإعدادات.", color = Ink.copy(alpha = .65f), fontSize = 11.sp)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("مواقيت اليوم", color = Navy, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(dateKey, color = Ink.copy(alpha = .6f), fontSize = 11.sp)
            }
            Button(onClick = { editTimes = manualTimes; showEntry = true }, colors = ButtonDefaults.buttonColors(containerColor = Navy)) {
                Icon(Icons.Default.Edit, null); Spacer(Modifier.width(5.dp)); Text("تعديل الأوقات")
            }
        }
        prayerNames.forEachIndexed { index, prayer ->
            Surface(color = Color.White, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(prayer, color = Navy, fontWeight = FontWeight.SemiBold)
                    Text(displayedTimes[index].ifBlank { "غير متاح" }, color = if (displayedTimes[index].isBlank()) Ink.copy(alpha = .45f) else Navy, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Text(
            if (manualTimes.any { it.isNotBlank() }) "الأوقات التي أدخلتها يدوياً تظهر بدلاً من الحساب. بقية الأوقات محسوبة فلكياً وليست جدولاً رسمياً محلياً."
            else "الأوقات المعروضة محسوبة فلكياً دون اتصال باستخدام إحداثيات الموقع وطريقة الحساب المختارة، وليست جدولاً رسمياً للجهة الدينية المحلية. اعتمد توقيت مسجدك أو الجهة الرسمية في بلدك عند توفره.",
            color = Ink.copy(alpha = .7f), fontSize = 12.sp, lineHeight = 19.sp
        )
        TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://dws.gov.iq/"))) }) {
            Icon(Icons.Default.OpenInNew, null); Spacer(Modifier.width(6.dp)); Text("فتح موقع ديوان الوقف السني")
        }
    }
}

private data class QuranAyah(val number: Int, val text: String)
private data class QuranSurah(val number: Int, val ayat: List<QuranAyah>)

private val arabicSurahNames = listOf(
"الفاتحة","البقرة","آل عمران","النساء","المائدة","الأنعام","الأعراف","الأنفال","التوبة","يونس","هود","يوسف","الرعد","إبراهيم","الحجر","النحل","الإسراء","الكهف","مريم","طه","الأنبياء","الحج","المؤمنون","النور","الفرقان","الشعراء","النمل","القصص","العنكبوت","الروم","لقمان","السجدة","الأحزاب","سبأ","فاطر","يس","الصافات","ص","الزمر","غافر","فصلت","الشورى","الزخرف","الدخان","الجاثية","الأحقاف","محمد","الفتح","الحجرات","ق","الذاريات","الطور","النجم","القمر","الرحمن","الواقعة","الحديد","المجادلة","الحشر","الممتحنة","الصف","الجمعة","المنافقون","التغابن","الطلاق","التحريم","الملك","القلم","الحاقة","المعارج","نوح","الجن","المزمل","المدثر","القيامة","الإنسان","المرسلات","النبأ","النازعات","عبس","التكوير","الانفطار","المطففين","الانشقاق","البروج","الطارق","الأعلى","الغاشية","الفجر","البلد","الشمس","الليل","الضحى","الشرح","التين","العلق","القدر","البينة","الزلزلة","العاديات","القارعة","التكاثر","العصر","الهمزة","الفيل","قريش","الماعون","الكوثر","الكافرون","النصر","المسد","الإخلاص","الفلق","الناس"
)

@Composable
private fun QuranScreen(
    modifier: Modifier = Modifier,
    isFullscreen: Boolean,
    onFullscreenChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    DisposableEffect(isFullscreen, activity) {
        val window = activity?.window
        val controller = window?.let { WindowInsetsControllerCompat(it, it.decorView) }
        if (isFullscreen) {
            controller?.hide(WindowInsetsCompat.Type.systemBars())
            controller?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            controller?.show(WindowInsetsCompat.Type.systemBars())
        }
        onDispose { controller?.show(WindowInsetsCompat.Type.systemBars()) }
    }

    val prefs = remember { context.getSharedPreferences("mihrab", Context.MODE_PRIVATE) }
    var targetDays by remember { mutableIntStateOf(prefs.getInt("khatmah_days", 30)) }
    var completedPages by remember { mutableIntStateOf(prefs.getInt("khatmah_pages", 0).coerceIn(0, 604)) }
    var currentPage by remember { mutableIntStateOf(prefs.getInt("quran_current_page", 1).coerceIn(1, 604)) }
    var savedPage by remember { mutableIntStateOf(prefs.getInt("quran_saved_page", 1).coerceIn(1, 604)) }
    var showJumpDialog by remember { mutableStateOf(false) }
    var jumpPageText by remember { mutableStateOf(currentPage.toString()) }
    var surahIndex by remember { mutableIntStateOf(prefs.getInt("quran_surah", 1).coerceIn(1, 114)) }
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

    if (showJumpDialog) {
        val requestedPage = jumpPageText.toIntOrNull()
        AlertDialog(
            onDismissRequest = { showJumpDialog = false },
            title = { Text("الانتقال إلى صفحة") },
            text = {
                OutlinedTextField(
                    value = jumpPageText,
                    onValueChange = { jumpPageText = it.filter(Char::isDigit).take(3) },
                    label = { Text("رقم الصفحة") },
                    placeholder = { Text("1 - 604") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = requestedPage == null || requestedPage !in 1..604
                )
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
        if (isFullscreen) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("الصفحة $currentPage / 604", color = Navy, fontWeight = FontWeight.Bold)
                IconButton(onClick = { onFullscreenChange(false) }) {
                    Icon(Icons.Default.FullscreenExit, contentDescription = "إنهاء ملء الشاشة", tint = Navy)
                }
            }
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("القرآن الكريم", color = Navy, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Text("مصحف المدينة المنورة — رواية حفص", color = Ink.copy(alpha = .65f), fontSize = 13.sp)
                }
                IconButton(onClick = { onFullscreenChange(true) }) {
                    Icon(Icons.Default.Fullscreen, contentDescription = "ملء الشاشة", tint = Navy)
                }
            }
            Surface(color = Color.White, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("الصفحات الأصلية للمصحف، بنفس ترتيب الآيات وتوزيع الأسطر والزخارف. تُحفظ داخل التطبيق للقراءة دون إنترنت.", color = Ink.copy(alpha = .75f), fontSize = 13.sp, lineHeight = 20.sp)
                    Text("اختر السورة", color = Navy, fontWeight = FontWeight.Bold)
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
                                    currentPage = quranPageStarts.indexOfLast {
                                        it.first < surahIndex || (it.first == surahIndex && it.second <= 1)
                                    }.coerceAtLeast(0) + 1
                                    prefs.edit().putInt("quran_surah", surahIndex).putInt("quran_current_page", currentPage).apply()
                                    surahMenu = false
                                })
                            }
                        }
                    }
                }
            }
        }

        val mushafBitmap = remember(currentPage) {
            runCatching {
                context.assets.open("mushaf_pages/$currentPage.png").use { BitmapFactory.decodeStream(it) }
            }.getOrNull()
        }
        if (mushafBitmap != null) {
            Image(
                bitmap = mushafBitmap.asImageBitmap(),
                contentDescription = "صفحة المصحف الشريف رقم $currentPage",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth().pointerInput(currentPage) {
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
            )
        } else {
            Text("تعذّر تحميل صورة الصفحة. أعد بناء التطبيق بعد تنزيل صفحات المصحف.", color = MaterialTheme.colorScheme.error)
        }

        if (!isFullscreen) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = { currentPage = (currentPage - 1).coerceAtLeast(1); prefs.edit().putInt("quran_current_page", currentPage).apply() }, enabled = currentPage > 1) {
                    Icon(Icons.Default.ChevronRight, null)
                    Text("السابقة")
                }
                Text("$currentPage / 604", color = Navy, fontWeight = FontWeight.Bold)
                Button(onClick = { currentPage = (currentPage + 1).coerceAtMost(604); prefs.edit().putInt("quran_current_page", currentPage).apply() }, enabled = currentPage < 604, colors = ButtonDefaults.buttonColors(containerColor = Navy)) {
                    Text("التالية")
                    Icon(Icons.Default.ChevronLeft, null)
                }
            }
            Text("صور صفحات المصحف: Quran Images — MIT؛ المصدر الأصلي للصور موضح في ملف الإسناد المرفق.", color = Ink.copy(alpha = .65f), fontSize = 11.sp)
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
                    Button(onClick = { completedPages = (completedPages + 1).coerceAtMost(604); prefs.edit().putInt("khatmah_pages", completedPages).apply() }, enabled = completedPages < 604, colors = ButtonDefaults.buttonColors(containerColor = Navy)) { Text("أكملت صفحة") }
                }
            }
            Text("موضع القراءة", color = Navy, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Surface(color = Color.White, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("آخر موضع محفوظ: صفحة $savedPage", color = Navy, fontWeight = FontWeight.SemiBold)
                    OutlinedButton(onClick = { jumpPageText = currentPage.toString(); showJumpDialog = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.FindInPage, null)
                        Spacer(Modifier.width(8.dp))
                        Text("الانتقال إلى رقم صفحة")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { savedPage = currentPage; prefs.edit().putInt("quran_saved_page", savedPage).apply() }, colors = ButtonDefaults.buttonColors(containerColor = Navy)) { Text("حفظ الموضع") }
                        OutlinedButton(onClick = { currentPage = savedPage; prefs.edit().putInt("quran_current_page", currentPage).apply() }) { Text("استرجاع المحفوظ") }
                    }
                    Text("موضعك الحالي يُحفظ تلقائياً أثناء التنقل.", color = Ink.copy(alpha = .6f), fontSize = 12.sp)
                }
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
    val dailyStorageKey = "${storageKey}_${java.time.LocalDate.now()}"
    var count by remember(dailyStorageKey) { mutableIntStateOf(prefs.getInt(dailyStorageKey, 0).coerceIn(0, target)) }
    Surface(color = Color.White, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text(phrase, color = Navy, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Text(source, color = Gold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("$count / $target", color = Navy, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Button(
                    onClick = {
                        count = (count + 1).coerceAtMost(target)
                        prefs.edit().putInt(dailyStorageKey, count).apply()
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
    val dailyStorageKey = "${storageKey}_${java.time.LocalDate.now()}"
    var isEvening by remember { mutableStateOf(false) }
    var count by remember(dailyStorageKey) { mutableIntStateOf(prefs.getInt(dailyStorageKey, 0).coerceIn(0, target)) }
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
                    prefs.edit().putInt(dailyStorageKey, count).apply()
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
private fun MoreScreen(modifier: Modifier = Modifier, darkMode: Boolean, onDarkModeChange: (Boolean) -> Unit) {
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
        Surface(color = if (darkMode) Color(0xFF202733) else Color.White, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("مظهر التطبيق", color = Navy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(if (darkMode) "المظهر الداكن مفعّل" else "المظهر الأبيض مفعّل", color = Ink.copy(alpha = .7f), fontSize = 12.sp)
                }
                Switch(checked = darkMode, onCheckedChange = onDarkModeChange)
            }
        }
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
        DonationSection()
        FeatureTile("التقويم الهجري", "التاريخ الهجري والمناسبات الثابتة", Icons.Default.CalendarMonth)
        FeatureTile("الإعدادات", "المظهر واللغة والموقع وطريقة عرض الوقت", Icons.Default.Settings)
        Text("مِحراب — تطبيق عربي يحترم خصوصيتك ويعمل دون خدمات سحابية مدفوعة.", color = Ink.copy(alpha = .65f), fontSize = 12.sp)
    }
}


@Composable
private fun DonationSection() {
    val context = LocalContext.current
    var amountText by remember { mutableStateOf("1000") }
    var error by remember { mutableStateOf("") }
    val quickAmounts = listOf(1000, 5000, 10000, 25000)
    val whatsappNumber = "9647709914494"
    Surface(color = Color.White, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            Surface(color = Navy, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = Gold, modifier = Modifier.size(34.dp))
                    Text("كن سبباً في فرحة محتاج", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text("ساهم بالمبلغ الذي يناسبك، وكل مساهمة تصنع فرقاً.", color = Color.White, fontSize = 13.sp, textAlign = TextAlign.Center)
                }
            }
            Text("كم تحب تساهم؟", color = Navy, fontWeight = FontWeight.Bold)
            quickAmounts.chunked(2).forEach { group -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { group.forEach { value -> FilterChip(selected = amountText == value.toString(), onClick = { amountText = value.toString(); error = "" }, label = { Text("$value د.ع") }, modifier = Modifier.weight(1f)) }; if (group.size == 1) Spacer(Modifier.weight(1f)) } }
            OutlinedTextField(value = amountText, onValueChange = { amountText = it.filter(Char::isDigit).take(10); error = "" }, label = { Text("مبلغ التبرع بالدينار العراقي") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
            Text("تگدر تساهم حتى بـ 1,000 دينار أو بأي مبلغ يناسبك.", color = Ink.copy(alpha = .7f), fontSize = 12.sp)
            Button(onClick = {
                val selected = amountText.toLongOrNull()
                if (selected == null || selected < 1000L) error = "أقل مساهمة 1,000 دينار." else {
                    val message = "السلام عليكم، أريد أساهم بمساعدة المحتاجين بمبلغ " + selected + " دينار عراقي. شلون أگدر أتبرع؟"
                    val uri = Uri.parse("https://wa.me/" + whatsappNumber + "?text=" + java.net.URLEncoder.encode(message, "UTF-8"))
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
            }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Navy)) {
                Icon(Icons.Default.Favorite, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("ساهم الآن عبر واتساب")
            }
            if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            Text("سيتم التواصل معك عبر واتساب لتنسيق المساهمة. التحويل لا يتم داخل التطبيق. تُدار المساعدات وتوزع شخصياً، ولسنا جمعية خيرية مسجلة.", color = Ink.copy(alpha = .65f), fontSize = 11.sp, lineHeight = 18.sp)
        }
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
