package com.pointcheck.core.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/**
 * Receptor de transmisiones (BroadcastReceiver) encargado de capturar alarmas programadas.
 * 
 * Se activa cuando el sistema dispara un Intent de recordatorio. Su función principal
 * es validar los permisos de notificación del usuario y desplegar una notificación
 * local en el sistema Android.
 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Primero, comprobamos si tenemos permiso para enviar notificaciones (Android 13+).
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Si no hay permiso, se cancela la operación silenciosamente.
            return
        }

        val title = intent.getStringExtra("title") ?: "Recordatorio de cita"
        val text = intent.getStringExtra("text") ?: "Tienes una reserva próximamente"
        val chanId = "pointcheck_reminders"

        // Configuración del canal de notificación (Requerido para API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(chanId, "Recordatorios", NotificationManager.IMPORTANCE_DEFAULT)
            val nm = context.getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }

        val noti = NotificationCompat.Builder(context, chanId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        // El ID de notificación se basa en el tiempo actual para evitar colisiones entre múltiples recordatorios.
        NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), noti)
    }
}
