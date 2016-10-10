package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {
    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHodler {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ViewHodler(View view) {
            this.iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            this.dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            this.descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            this.highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            this.lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }

    /*
        Remember that these views are reused as needed.
     */

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = (viewType == VIEW_TYPE_TODAY) ? R.layout.list_item_forecast_today : R.layout.list_item_forecast;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHodler viewHodler = new ViewHodler(view);
        view.setTag(viewHodler);
        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        String description=cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        ViewHodler viewHodler = (ViewHodler) view.getTag();
        //imageIcon
        int viewType=getItemViewType(cursor.getPosition()); ;
        viewHodler.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(weatherId,viewType));
        viewHodler.iconView.setContentDescription(description);
        //date
        viewHodler.dateView.setText(Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE)));
        //description
        viewHodler.descriptionView.setText(description);
        //high And low temp
        boolean isMetric = Utility.isMetric(context);
        String format_highTemp = Utility.formatTemperature(context, cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP), isMetric);
        String format_lowTemp = Utility.formatTemperature(context, cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP), isMetric);
        viewHodler.highTempView.setText(format_highTemp);
        viewHodler.lowTempView.setText(format_lowTemp);
    }

    static final int VIEW_TYPE_TODAY = 0;
    static final int VIEW_TYPE_FUTURE = 1;
    public static boolean mUseTodayLayout;
    @Override
    public int getItemViewType(int position) {
        return (position == 0&& !mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }
    public  void setUseTodayLayout(boolean useTodayLayout){
        mUseTodayLayout=useTodayLayout;
    }
}