package com.udacity.stockhawk.widget;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import timber.log.Timber;

public class WidgetService extends RemoteViewsService {

    private static final String[] COLUMNS = Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{});
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        final DecimalFormat dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        final DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        final DecimalFormat percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
        //Timber.d("Remote Views Facotry");
        return new RemoteViewsFactory() {
            private Cursor data = null;
            @Override
            public void onCreate() {
            }

            @Override
            public void onDataSetChanged() {
                Timber.d("Data was changed");
                if (data != null) {
                    data.close();
                }
                final long identityToken = Binder.clearCallingIdentity();

                Uri stocksUri = Contract.Quote.URI;
                data = getContentResolver().query(stocksUri, COLUMNS, null, null, null);
                
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                //Timber.d("getCount: " + data.getCount());
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                //Timber.d("GetViewAt " + position);
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_list_item);
                String symbol = data.getString(Contract.Quote.POSITION_SYMBOL);
                Float price = data.getFloat(Contract.Quote.POSITION_PRICE);
                Float absChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
                Float prsChange = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);


                views.setTextViewText(R.id.symbol, symbol);
                views.setTextViewText(R.id.price, dollarFormat.format(price));
                views.setTextViewText(R.id.change, dollarFormatWithPlus.format(absChange));

                if(absChange > 0){
                    views.setInt(R.id.change,"setBackgroundResource", R.drawable.percent_change_pill_green);
                }else{
                    views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
                }

                Context context = getApplicationContext();
                if (PrefUtils.getDisplayMode(context)
                        .equals(context.getString(R.string.pref_display_mode_absolute_key))) {
                    views.setTextViewText(R.id.change, dollarFormatWithPlus.format(absChange));
                }else{
                    views.setTextViewText(R.id.change, percentageFormat.format(prsChange/100));
                }

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                //Timber.d("GetItemId: " + position);
                if (data.moveToPosition(position))
                    return data.getLong(Contract.Quote.POSITION_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }


        };
    }
}
