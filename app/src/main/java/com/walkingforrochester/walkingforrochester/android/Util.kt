package com.walkingforrochester.walkingforrochester.android

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import com.walkingforrochester.walkingforrochester.android.service.ForegroundLocationService.Companion.NOTIFICATION_CHANNEL_ID
import com.walkingforrochester.walkingforrochester.android.service.ForegroundLocationService.Companion.NOTIFICATION_ID
import java.math.BigInteger
import java.security.MessageDigest
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.math.roundToInt

@SuppressLint("MissingPermission")
fun showNotification(context: Context, text: String) {
    val launchActivityIntent = Intent(context, MainActivity::class.java)
    val activityPendingIntent = PendingIntent.getActivity(
        context,
        0,
        launchActivityIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(context.getString(R.string.app_name))
        .setContentText(text)
        .setContentIntent(activityPendingIntent)
        .setDefaults(NotificationCompat.DEFAULT_ALL)
        .setAutoCancel(true)

    with(NotificationManagerCompat.from(context)) {
        notify(NOTIFICATION_ID, builder.build())
    }
}

fun getSharedPreferences(context: Context): SharedPreferences = context.getSharedPreferences(
    context.getString(R.string.wfr_preferences),
    Context.MODE_PRIVATE
)

fun savePreference(context: Context, key: String, value: Any) =
    with(getSharedPreferences(context).edit()) {
        when (value) {
            is Boolean -> putBoolean(key, value)
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is Float -> putFloat(key, value)
            is String -> putString(key, value)
            else -> error("Cannot save preference of type ${value.javaClass}, try using SharedPreferences SDK methods")
        }
        apply()
    }

fun getPreference(context: Context, key: String, defaultValue: Any): Any? =
    with(getSharedPreferences(context)) {
        when (defaultValue) {
            is Boolean -> getBoolean(key, defaultValue)
            is Int -> getInt(key, defaultValue)
            is Long -> getLong(key, defaultValue)
            is Float -> getFloat(key, defaultValue)
            is String -> getString(key, defaultValue)
            else -> error("Cannot get preference of type ${defaultValue.javaClass}, try using SharedPreferences SDK methods")
        }
    }

fun roundDouble(d: Double?): Double = ((d ?: 0.0) * 100.0).roundToInt() / 100.0

fun metersToMiles(d: Double?): Double = (d ?: 0.0) * 0.000621371192

fun md5(input: String): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
}

class LocalDateAdapter {
    @ToJson
    fun toJson(value: LocalDate): String {
        return value.toString()
    }

    @FromJson
    fun fromJson(value: String): LocalDate {
        formatters.forEach { formatter ->
            try {
                return LocalDate.parse(value, formatter)
            } catch (dtpe: DateTimeParseException) {
                //
            }
        }

        throw RuntimeException("Unable to parse date: $value")
    }

    companion object {
        val formatters =
            listOf(DateTimeFormatter.ISO_LOCAL_DATE, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }
}

class WFRDateFormatter {
    companion object {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM_dd_yyyy")
    }
}
