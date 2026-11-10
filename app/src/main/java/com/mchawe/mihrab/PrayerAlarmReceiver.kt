package com.mchawe.mihrab

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object PrayerAlarmScheduler {
    private val prayerKeys = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
    private val prayerNames = listOf("الفجر", "الظهر", "العصر", "المغرب", "العشاء")

    fun schedule(context: Context) {
        val prefs = context.getSharedPreferences("mihrab", Context.MODE_PRIVATE)
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!prefs.getBoolean("global_adhan_enabled", false)) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !manager.canScheduleExactAlarms()) return
        val latitude = prefs.getFloat("last_latitude", 35.4681f).toDouble()
        val longitude = prefs.getFloat("last_longitude", 44.3922f).toDouble()
        val zoneId = prefs.getString("selected_zone", ZoneId.systemDefault().id) ?: ZoneId.systemDefault().id
        val methodName = prefs.getString("global_method", GlobalPrayerCalculator.Method.MUSLIM_WORLD_LEAGUE.name)
        val asrName = prefs.getString("asr_method", GlobalPrayerCalculator.AsrMethod.SHAFII.name)
        val method = GlobalPrayerCalculator.Method.values().firstOrNull { it.name == methodName } ?: GlobalPrayerCalculator.Method.MUSLIM_WORLD_LEAGUE
        val asr = GlobalPrayerCalculator.AsrMethod.values().firstOrNull { it.name == asrName } ?: GlobalPrayerCalculator.AsrMethod.SHAFII
        val zone = try { ZoneId.of(zoneId) } catch (_: Exception) { ZoneId.systemDefault() }
        val now = ZonedDateTime.now(zone)
        for (dayOffset in 0..7) {
            val date = now.toLocalDate().plusDays(dayOffset.toLong())
            val times = GlobalPrayerCalculator.calculate(latitude, longitude, date, zone.id, method, asr) ?: continue
            val values = listOf(times.fajr, times.dhuhr, times.asr, times.maghrib, times.isha)
            values.forEachIndexed { index, value ->
                if (!prefs.getBoolean("adhan_enabled_${prayerKeys[index]}", true)) return@forEachIndexed
                val parsed = try { java.time.LocalTime.parse(value, DateTimeFormatter.ofPattern("HH:mm")) } catch (_: Exception) { return@forEachIndexed }
                val trigger = date.atTime(parsed).atZone(zone).toInstant().toEpochMilli()
                if (trigger <= System.currentTimeMillis()) return@forEachIndexed
                val id = ((date.toEpochDay() % 100000) * 10 + index).toInt()
                val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                    action = "com.mchawe.mihrab.PRAYER_ALARM"
                    putExtra("prayer_name", prayerNames[index])
                    putExtra("alarm_id", id)
                }
                val pending = PendingIntent.getBroadcast(context, id, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pending)
                else manager.setExact(AlarmManager.RTC_WAKEUP, trigger, pending)
            }
        }
    }

    fun cancelAll(context: Context) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val prefs = context.getSharedPreferences("mihrab", Context.MODE_PRIVATE)
        val keys = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
        val zone = try { ZoneId.of(prefs.getString("selected_zone", ZoneId.systemDefault().id)!!) } catch (_: Exception) { ZoneId.systemDefault() }
        for (offset in 0..7) for (index in keys.indices) {
            val date = LocalDate.now(zone).plusDays(offset.toLong())
            val id = ((date.toEpochDay() % 100000) * 10 + index).toInt()
            val intent = Intent(context, PrayerAlarmReceiver::class.java).apply { action = "com.mchawe.mihrab.PRAYER_ALARM" }
            val pending = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
            if (pending != null) manager.cancel(pending)
        }
    }
}

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val name = intent.getStringExtra("prayer_name") ?: "الصلاة"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "mihrab_prayer_adhan"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (manager.getNotificationChannel(channelId) == null) {
                val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                val attributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
                val channel = NotificationChannel(channelId, "تنبيهات مواقيت الصلاة", NotificationManager.IMPORTANCE_HIGH)
                channel.description = "تنبيه عند دخول وقت الصلاة"
                channel.setSound(sound, attributes)
                manager.createNotificationChannel(channel)
            }
        }
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_mihrab_launcher)
            .setContentTitle("حان وقت صلاة $name")
            .setContentText("حيّ على الصلاة • مِحراب")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()
        try { manager.notify(intent.getIntExtra("alarm_id", 1001), notification) } catch (_: SecurityException) { }
        PrayerAlarmScheduler.schedule(context)
    }
}

class PrayerBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) PrayerAlarmScheduler.schedule(context)
    }
}
