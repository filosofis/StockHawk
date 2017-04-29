package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.ui.MainActivity;

import timber.log.Timber;

/**
 * Implementation of App Widget functionality.
 */
public class WidgetProvider extends AppWidgetProvider {


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //Timber.d("onUpdate");
        for (int appWidgetId : appWidgetIds) {
            //Timber.d("onUpdate" + context.getPackageName());
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.stock_widget);
            Intent intent = new Intent(context, WidgetService.class);
            rv.setRemoteAdapter(R.id.stock_list, intent);

            Intent stockHawkActivityIntent = new Intent(context, MainActivity.class);
            PendingIntent activityPendingIntent = PendingIntent.getActivity(context, 0, stockHawkActivityIntent, 0);
            rv.setPendingIntentTemplate(R.id.stock_list, activityPendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, rv);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //Timber.d("onRecive");
        super.onReceive(context, intent);
        if(QuoteSyncJob.ACTION_DATA_UPDATED.equals(intent.getAction())){
            //Timber.d("ACTION_DATA_UPDATED");
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, getClass()));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.stock_list);
        }
    }
}
