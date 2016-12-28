package com.gellert.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gellert.sunshine.data.WeatherContract;
import com.gellert.sunshine.data.WeatherContract.WeatherEntry;

public class DetailFragment extends Fragment  implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static String LOG_TAG = DetailFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";

    final static String FORECAST_SHARE_HASHTAG = " #MyWeatherApp";
    static final String DETAIL_TRANSITION_ANIMATION = "DTA";

    private boolean mTransitionAnimation = false;

    //private ShareActionProvider mShareActionProvider;
    private String mForecast;
    private Uri mUri;

    private final static int DETAILS_LOADER_ID = 0;


    private static final String[] DETAIL_COLUMNS = {
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATE,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_HUMIDITY,
            WeatherEntry.COLUMN_PRESSURE,
            WeatherEntry.COLUMN_WIND_SPEED,
            WeatherEntry.COLUMN_DEGREES,
            WeatherEntry.COLUMN_WEATHER_ID,

            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
    };

    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_WEATHER_HUMIDITY = 5;
    public static final int COL_WEATHER_PRESSURE = 6;
    public static final int COL_WEATHER_WIND_SPEED = 7;
    public static final int COL_WEATHER_DEGREES = 8;
    public static final int COL_WEATHER_CONDITION_ID = 9;

    private ImageView mIconView;
    private TextView mDateView;
    private TextView mDescriptionView;
    private TextView mHighView;
    private TextView mLowView;
    private TextView mHumidityView;
    private TextView mHumidityLabelView;
    private TextView mPressureView;
    private TextView mPressureLabelView;
    private TextView mWindView;
    private TextView mWindLabelView;
    //private CompassView mCompassView;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
            mTransitionAnimation = arguments.getBoolean(DetailFragment.DETAIL_TRANSITION_ANIMATION, false);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail_start,container,false);

        mIconView = (ImageView)rootView.findViewById(R.id.detail_icon_imageview);
        mDateView = (TextView)rootView.findViewById(R.id.detail_date_textview);
        mDescriptionView = (TextView)rootView.findViewById(R.id.detail_description_textview);
        mHighView = (TextView)rootView.findViewById(R.id.detail_high_textview);
        mLowView = (TextView)rootView.findViewById(R.id.detail_low_textview);
        mHumidityView = (TextView)rootView.findViewById(R.id.detail_humidity_textview);
        mHumidityLabelView = (TextView)rootView.findViewById(R.id.detail_humidity_label_textview);
        mPressureView = (TextView)rootView.findViewById(R.id.detail_pressure_textview);
        mPressureLabelView = (TextView)rootView.findViewById(R.id.detail_pressure_label_textview);
        mWindView = (TextView)rootView.findViewById(R.id.detail_wind_textview);
        mWindLabelView = (TextView)rootView.findViewById(R.id.detail_wind_label_textview);
        //mCompassView = (CompassView)rootView.findViewById(R.id.detail_wind_compassview);

        return rootView;
    }

    private void finishCreatingMenu(Menu menu) {
        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);
        menuItem.setIntent(createShareForecastIntent());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        if(getActivity() instanceof DetailActivity) {
            inflater.inflate(R.menu.detailfragment, menu);
            finishCreatingMenu(menu);
        }
    }

    public Intent createShareForecastIntent(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAILS_LOADER_ID,null,this);
        super.onActivityCreated(savedInstanceState);
    }

    void onLocationChanged( String newLocation ) {
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAILS_LOADER_ID, null, this);
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if ( null != mUri ) {
            return new CursorLoader(getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }

        ViewParent vp = getView().getParent();
        if ( vp instanceof CardView ) {
            ((View)vp).setVisibility(View.INVISIBLE);
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Log.i(LOG_TAG,String.valueOf(data.moveToFirst()));
        if(data != null && data.moveToFirst()){
            ViewParent vp = getView().getParent();
            if ( vp instanceof CardView) {
                ((View)vp).setVisibility(View.VISIBLE);
            }
            int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);

            if ( Utility.usingLocalGraphics(getActivity()) ) {
                mIconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
            } else {
                // Use weather art image
                Glide.with(this)
                        .load(Utility.getArtUrlForWeatherCondition(getActivity(), weatherId))
                        .error(Utility.getArtResourceForWeatherCondition(weatherId))
                        .crossFade()
                        .into(mIconView);
            }

            long date = data.getLong(COL_WEATHER_DATE);
            String dateText = Utility.getFullFriendlyDayString(getActivity(),date);
            mDateView.setText(dateText);


            String description = data.getString(COL_WEATHER_DESC);
            mDescriptionView.setText(description);
            mIconView.setContentDescription(description);

            boolean isMetric = Utility.isMetric(getActivity());


            double high = data.getDouble(COL_WEATHER_MAX_TEMP);
            String highString = Utility.formatTemperature(getActivity(),high,isMetric);
            mHighView.setText(highString);
            mHighView.setContentDescription(highString);


            double low = data.getDouble(COL_WEATHER_MIN_TEMP);
            String lowString = Utility.formatTemperature(getActivity(),low,isMetric);
            mLowView.setText(lowString);
            mLowView.setContentDescription(lowString);

            float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
            mHumidityView.setText(getActivity().getString(R.string.format_humidity,humidity));
            mHumidityView.setContentDescription(mHumidityView.getText());
            mHumidityLabelView.setContentDescription(mHumidityView.getText());

            float windSpeed = data.getFloat(COL_WEATHER_WIND_SPEED);
            float windDirection = data.getFloat(COL_WEATHER_DEGREES);
            mWindView.setText(Utility.getFormattedWind(getActivity(),windSpeed,windDirection));
            mWindView.setContentDescription(mWindView.getText());
            mWindLabelView.setContentDescription(mWindView.getText());

            //mCompassView.setDirection(windDirection);
            //mCompassView.setContentDescription("Wind direction " + windDirection);

            float pressure = data.getFloat(COL_WEATHER_PRESSURE);
            mPressureView.setText(getActivity().getString(R.string.format_pressure,pressure));
            mPressureView.setContentDescription(mPressureView.getText());
            mPressureLabelView.setContentDescription(mPressureView.getText());

            mForecast = String.format("%s - %s - %s/%s", dateText, description, high, low);
        }

        AppCompatActivity activity = (AppCompatActivity)getActivity();
        Toolbar toolbarView = (Toolbar) getView().findViewById(R.id.toolbar);

        // We need to start the enter transition after the data has loaded
        if (mTransitionAnimation) {
            activity.supportStartPostponedEnterTransition();

            if ( null != toolbarView ) {
                activity.setSupportActionBar(toolbarView);

                activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        } else {
            if ( null != toolbarView ) {
                Menu menu = toolbarView.getMenu();
                if ( null != menu ) menu.clear();
                toolbarView.inflateMenu(R.menu.detailfragment);
                finishCreatingMenu(toolbarView.getMenu());
            }
        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}
}
