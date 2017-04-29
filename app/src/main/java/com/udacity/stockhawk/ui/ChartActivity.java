package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.os.Bundle;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChartActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor>
{
    List<Entry> entries = new ArrayList<>();
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String symbol = getIntent().getStringExtra("symbol");
        Uri getStockUri = Contract.Quote.makeUriForStock(symbol);
        return new CursorLoader(this, getStockUri,null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor cursor) {

        cursor.moveToFirst();
        LineChart chart = (LineChart) findViewById(R.id.chart);
        //int historyIndex = cursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY);
        final String[] historyRaw = cursor.getString(Contract.Quote.POSITION_HISTORY).split("\n");
        String[] history;


        for(int i=0; i<historyRaw.length; i++){
            //Timber.d(historyRaw[i]);
            history = historyRaw[i].split(",");
            //Timber.d(history[0] + " " + history[1]);
            entries.add(new Entry(i,Float.parseFloat(history[1])));
        }
        //Although its no longer actually displayed
        LineDataSet dataSet = new LineDataSet(entries, getString(R.string.data_set_label));
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);

        chart.getAxisLeft().setTextColor(Color.WHITE);
        chart.getAxisRight().setDrawLabels(false);
        XAxis xAxis = chart.getXAxis();
        xAxis.setLabelCount(5);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                //The ugliest line of code I've ever written
                Date date = new Date(Long.parseLong(historyRaw[(int)value].split(",")[0]));
                SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());
                //Timber.d(shortenedDateFormat.format(date));
                return shortenedDateFormat.format(date);
            }
        });
        chart.invalidate();
        cursor.close();
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        getSupportLoaderManager().initLoader(1, null, this);
        //String symbol = getIntent().getStringExtra("symbol");
        //Timber.d(symbol);
        //Uri getStockUri = Contract.Quote.makeUriForStock(symbol);
        //Cursor cursor = this.getContentResolver().query(getStockUri, null, null, null, null, null);

    }

    static String formatDate(long dateInMilliseconds) {
        Date date = new Date(dateInMilliseconds);
        return DateFormat.getDateInstance().format(date);
    }

    // Format used for storing dates in the database.  ALso used for converting those strings
    // back into date objects for comparison/processing.
    public static final String DATE_FORMAT = "yyyyMMdd";
}
