package com.tasomaniac.devwidget.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.os.Bundle
import com.tasomaniac.devwidget.data.Widget
import com.tasomaniac.devwidget.data.WidgetDao
import com.tasomaniac.devwidget.data.deleteWidgets
import com.tasomaniac.devwidget.data.updater.WidgetRefreshService
import com.tasomaniac.devwidget.extensions.SchedulingStrategy
import com.tasomaniac.devwidget.extensions.flatten
import com.tasomaniac.devwidget.settings.AutoUpdatePreferences
import dagger.android.AndroidInjection
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class WidgetProvider : AppWidgetProvider() {

    @Inject lateinit var widgetDao: WidgetDao
    @Inject lateinit var scheduling: SchedulingStrategy
    @Inject lateinit var widgetUpdater: WidgetUpdater
    @Inject lateinit var autoUpdatePreferences: AutoUpdatePreferences

    private val disposables = CompositeDisposable()

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)
        super.onReceive(context, intent)

        if (SDK_INT >= O && autoUpdatePreferences.autoUpdate) {
            context.startForegroundService(Intent(context, WidgetRefreshService::class.java))
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, vararg appWidgetIds: Int) {
        if (appWidgetIds.isEmpty()) return

        val pendingResult = goAsync()

        disposables.clear()
        disposables.add(widgetDao.findWidgetsById(appWidgetIds)
            .flatten()
            .flatMapCompletable {
                widgetUpdater.update(it)
            }
            .compose(scheduling.forCompletable())
            .subscribe(pendingResult::finish))
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        val pendingResult = goAsync()

        disposables.clear()
        disposables.add(widgetDao.findWidgetById(appWidgetId)
            .flatMapCompletable {
                widgetUpdater.update(it, newOptions)
            }
            .compose(scheduling.forCompletable())
            .subscribe(pendingResult::finish))
    }

    override fun onDeleted(context: Context, vararg appWidgetIds: Int) {
        val widgets = appWidgetIds.map { Widget(it) }
        widgetDao.deleteWidgets(widgets)
            .compose(scheduling.forCompletable())
            .subscribe()
    }
}
