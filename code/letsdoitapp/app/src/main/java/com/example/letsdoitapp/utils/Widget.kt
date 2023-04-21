package com.example.letsdoitapp.utils

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.letsdoitapp.activity.MainActivity.Companion.chronoWidget
import com.example.letsdoitapp.activity.MainActivity.Companion.distanceWidget
import com.example.letsdoitapp.R

class Widget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        if (appWidgetIds != null && appWidgetManager != null && context != null) {
            for (appWidgetId in appWidgetIds) {

                val views = RemoteViews(context.packageName, R.layout.widget)

                views.setTextViewText(R.id.tvWidgetChrono, chronoWidget)
                views.setTextViewText(R.id.tvWidgetDistance, distanceWidget)

                appWidgetManager.updateAppWidget(appWidgetId, views)

            }
        }


    }

}