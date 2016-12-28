package com.gellert.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gellert.sunshine.data.WeatherContract;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder> {
    private static final String TAG = "ForecastAdapter";

    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_FUTURE = 1;

    private boolean mUseTodayLayout = true;

    private Cursor mCursor;
    private Context mContext;
    final private ForecastAdapterOnClickHandler mClickHandler;
    final private View mEmptyView;
    final private ItemChoiceManager mICM;

    private int mPosition;

    public class ForecastAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView mIconView;
        final TextView mDateView;
        final TextView mDescriptionView;
        final TextView mHighTempView;
        final TextView mLowTempView;
        final View parentView;

        public ForecastAdapterViewHolder(View view) {
            super(view);
            mIconView = (ImageView)view.findViewById(R.id.list_item_icon);
            mDateView = (TextView)view.findViewById(R.id.list_item_date_textview);
            mDescriptionView = (TextView)view.findViewById(R.id.list_item_forecast_textview);
            mHighTempView = (TextView)view.findViewById(R.id.list_item_high_textview);
            mLowTempView = (TextView)view.findViewById(R.id.list_item_low_textview);

            view.setOnClickListener(this);
            view.setClickable(true);
            parentView = view;
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int dateColumnIndex = mCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
            mClickHandler.onClick(mCursor.getLong(dateColumnIndex), this);
            mICM.onClick(this);
        }
    }

    public static interface ForecastAdapterOnClickHandler{
        void onClick(Long date, ForecastAdapterViewHolder vh);
    }

    public ForecastAdapter(Context context, ForecastAdapterOnClickHandler dh, View emptyView, int choiceMode) {
        mContext = context;
        mClickHandler = dh;
        mEmptyView = emptyView;
        mICM = new ItemChoiceManager(this);
        mICM.setChoiceMode(choiceMode);
    }


    void setUseTodayLayout(boolean useTodayLayout){
        mUseTodayLayout = useTodayLayout;
    }

    @Override
    public ForecastAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if(viewGroup instanceof RecyclerView){
            int layoutId = -1;
            switch (viewType) {
                case VIEW_TYPE_TODAY:
                    layoutId = R.layout.list_item_forecast_today;
                    break;
                case VIEW_TYPE_FUTURE:
                    layoutId = R.layout.list_item_forecast;
                    break;
            }

            View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutId,viewGroup,false);
            view.setFocusable(true);
            return new ForecastAdapterViewHolder(view);
        } else{
            throw new RuntimeException("Not bound to RecyclerViewSelection");
        }
    }

    @Override
    public void onBindViewHolder(ForecastAdapterViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        // Read weather icon ID from mCursor
        int weatherId = mCursor.getInt(mCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID));
        int defaultImage;

        switch (getItemViewType(mCursor.getPosition())){
            case VIEW_TYPE_TODAY:
                defaultImage = Utility.getArtResourceForWeatherCondition(weatherId);
                break;
            default:
                defaultImage = Utility.getIconResourceForWeatherCondition(weatherId);
                break;
        }

        if(Utility.usingLocalGraphics(mContext)){
            holder.mIconView.
                    setImageResource(defaultImage);
        } else {
            Glide.with(mContext)
                    .load(Utility.getArtUrlForWeatherCondition(mContext, weatherId))
                    .error(defaultImage)
                    .crossFade()
                    .into(holder.mIconView);
        }
        ViewCompat.setTransitionName(holder.mIconView,"iconView" + position);

        // Read date from mCursor
        long date = mCursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        holder.mDateView.setText(Utility.getFriendlyDayString(mContext,date));

        // Read weather forecast from mCursor
        String weatherDesc = mCursor.getString(ForecastFragment.COL_WEATHER_DESC);
        holder.mDescriptionView.setText(weatherDesc);
        holder.mDescriptionView.setContentDescription(weatherDesc);
        holder.mIconView.setContentDescription(weatherDesc);

        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(mContext);

        // Read high temperature from mCursor
        double high = mCursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        holder.mHighTempView.setText(Utility.formatTemperature(mContext, high, isMetric));
        holder.mHighTempView.setContentDescription(holder.mHighTempView.getText());

        // Read low temperature from mCursor
        double low = mCursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        holder.mLowTempView.setText(Utility.formatTemperature(mContext, low, isMetric));
        holder.mLowTempView.setContentDescription(holder.mLowTempView.getText());

        mICM.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE;
    }

    @Override
    public int getItemCount() {
        if(null == mCursor) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor c) {
        mCursor = c;
        notifyDataSetChanged();
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public void selectView(RecyclerView.ViewHolder viewHolder) {
        if ( viewHolder instanceof ForecastAdapterViewHolder ) {
            ForecastAdapterViewHolder vfh = (ForecastAdapterViewHolder)viewHolder;
            vfh.onClick(vfh.itemView);
        }
    }

    public int getSelectedItemPosition() {
        return mICM.getSelectedItemPosition();
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mICM.onRestoreInstanceState(savedInstanceState);
    }

    public void onSaveInstanceState(Bundle outState) {
        mICM.onSaveInstanceState(outState);
    }
}