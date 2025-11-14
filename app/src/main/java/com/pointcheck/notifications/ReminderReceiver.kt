package com.pointcheck.notifications

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

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Primero, comprobamos si tenemos permiso para enviar notificaciones.
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Si no hay permiso, simplemente no hacemos nada para evitar que la app se cierre.
            return
        }

        val title = intent.getStringExtra("title") ?: "Recordatorio de cita"
        val text = intent.getStringExtra("text") ?: "Tienes una reserva próximamente"
        val chanId = "pointcheck_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(chanId, "Recordatorios", NotificationManager.IMPORTANCE_DEFAULT)
            val nm = context.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }

        val noti = NotificationCompat.Builder(context, chanId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), noti)
    }
}
