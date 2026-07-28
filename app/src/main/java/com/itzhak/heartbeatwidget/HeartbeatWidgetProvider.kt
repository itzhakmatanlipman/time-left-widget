package com.itzhak.heartbeatwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class HeartbeatWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // מצייר מיד, וגם מוודא שהשירות שמתקתק כל שנייה רץ
        updateAll(context, appWidgetManager, appWidgetIds)
        HeartbeatService.start(context)
    }

    override fun onEnabled(context: Context) {
        // נקרא כשמוסיפים את הווידג'ט הראשון
        HeartbeatService.start(context)
    }

    override fun onDisabled(context: Context) {
        // נקרא כשמוסר הווידג'ט האחרון — עוצרים את השירות
        HeartbeatService.stop(context)
    }

    companion object {
        /** מצייר את הווידג'ט מחדש ודוחף לכל המופעים על המסך. */
        fun updateAll(
            context: Context,
            manager: AppWidgetManager = AppWidgetManager.getInstance(context),
            ids: IntArray = manager.getAppWidgetIds(
                ComponentName(context, HeartbeatWidgetProvider::class.java)
            )
        ) {
            if (ids.isEmpty()) return

            val result = HeartbeatCalc.compute()
            val bitmap = WidgetRenderer.render(result)

            val views = RemoteViews(context.packageName, R.layout.widget_heartbeat)
            views.setImageViewBitmap(R.id.widget_image, bitmap)

            // הקשה על הווידג'ט פותחת את האפליקציה
            val intent = Intent(context, MainActivity::class.java)
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val pi = PendingIntent.getActivity(context, 0, intent, flags)
            views.setOnClickPendingIntent(R.id.widget_image, pi)

            for (id in ids) {
                manager.updateAppWidget(id, views)
            }
        }
    }
}
