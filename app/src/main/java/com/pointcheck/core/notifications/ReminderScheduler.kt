package com.pointcheck.core.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

/**
 * Programador de recordatorios locales utilizando [AlarmManager].
 * 
 * Permite agendar notificaciones exactas en el tiempo (epoch millis). Maneja las restricciones
 * de Android 12+ sobre alarmas exactas y asegura que la aplicación no sufra cierres inesperados
 * por falta de permisos de sistema.
 *
 * @property context Contexto de la aplicación necesario para acceder a los servicios del sistema.
 */
class ReminderScheduler(private val context: Context) {

    /**
     * Programa una notificación para ser disparada en un momento específico.
     *
     * @param epochMillis Tiempo en milisegundos cuando se debe activar la alarma.
     * @param title Título de la notificación.
     * @param text Cuerpo del mensaje de la notificación.
     */
    fun scheduleAt(epochMillis: Long, title: String, text: String) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Validación de permisos para alarmas exactas (Requerido en Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!am.canScheduleExactAlarms()) {
                Log.w("ReminderScheduler", "No se pueden programar alarmas exactas. Permiso denegado.")
                return
            }
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("text", text)
        }

        // Se usa el remanente de epochMillis como ID único para el PendingIntent
        val req = (epochMillis % Int.MAX_VALUE).toInt()
        val pi = PendingIntent.getBroadcast(context, req, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        try {
            // Se utiliza setExactAndAllowWhileIdle para asegurar que el recordatorio suene 
            // incluso si el dispositivo está en modo Doze (ahorro de energía).
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, epochMillis, pi)
        } catch (e: SecurityException) {
            Log.e("ReminderScheduler", "Error de seguridad al programar la alarma.", e)
        }
    }
}
