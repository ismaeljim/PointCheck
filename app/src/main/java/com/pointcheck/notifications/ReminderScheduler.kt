package com.pointcheck.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class ReminderScheduler(private val context: Context) {
    fun scheduleAt(epochMillis: Long, title: String, text: String) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // En Android 12 y superior, se necesita un permiso especial para alarmas exactas.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!am.canScheduleExactAlarms()) {
                // No tenemos permiso, así que no hacemos nada. La app no se cerrará.
                Log.w("ReminderScheduler", "No se pueden programar alarmas exactas. Permiso denegado.")
                return
            }
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("text", text)
        }

        val req = (epochMillis % Int.MAX_VALUE).toInt()
        val pi = PendingIntent.getBroadcast(context, req, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        try {
            // Envolvemos la llamada en un try-catch para máxima seguridad.
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, epochMillis, pi)
        } catch (e: SecurityException) {
            // Si el sistema operativo deniega la alarma, lo capturamos y evitamos el crash.
            Log.e("ReminderScheduler", "Error de seguridad al programar la alarma.", e)
        }
    }
}
