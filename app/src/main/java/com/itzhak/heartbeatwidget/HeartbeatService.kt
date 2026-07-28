package com.itzhak.heartbeatwidget

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager

/**
 * מריץ טיקר שמעדכן את הווידג'ט כל שנייה — אבל רק כשהמסך דולק.
 * כשהמסך נכבה הטיקר נעצר, כך שכשהטלפון בכיס אין שום צריכת סוללה.
 */
class HeartbeatService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var ticking = false

    // הטיקר: מצייר מחדש ומזמן את עצמו שוב בעוד שנייה
    private val tick = object : Runnable {
        override fun run() {
            HeartbeatWidgetProvider.updateAll(applicationContext)
            handler.postDelayed(this, 1000L)
        }
    }

    // מאזין להדלקה/כיבוי מסך
    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON -> startTicking()
                Intent.ACTION_SCREEN_OFF -> stopTicking()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIF_ID, buildNotification())

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenReceiver, filter)

        // אם המסך כבר דולק כרגע — מתחילים לתקתק מיד
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (pm.isInteractive) startTicking()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // אם המערכת הרגה את השירות, שידלק מחדש
        return START_STICKY
    }

    private fun startTicking() {
        if (ticking) return
        ticking = true
        handler.post(tick)
    }

    private fun stopTicking() {
        ticking = false
        handler.removeCallbacks(tick)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTicking()
        try {
            unregisterReceiver(screenReceiver)
        } catch (_: Exception) {
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Heartbeat widget",
                NotificationManager.IMPORTANCE_MIN // הכי שקט וזניח שאפשר
            )
            channel.setShowBadge(false)
            manager.createNotificationChannel(channel)
        }
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        return builder
            .setContentTitle("Heartbeat widget פעיל")
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "heartbeat_widget"
        private const val NOTIF_ID = 1

        fun start(context: Context) {
            val intent = Intent(context, HeartbeatService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, HeartbeatService::class.java))
        }
    }
}
