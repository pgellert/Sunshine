package com.gellert.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.gellert.sunshine.gcm.RegistrationIntentService;
import com.gellert.sunshine.sync.SunshineSyncAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity implements ForecastFragment.Callback{

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";

    private String mLocation;
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocation = Utility.getPreferredLocation(this);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if(findViewById(R.id.weather_detail_container) != null){
            mTwoPane = true;

            if(savedInstanceState == null){
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container,new DetailFragment(),DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else{
            mTwoPane = false;
        }

        ForecastFragment forecastFragment =  ((ForecastFragment)(getSupportFragmentManager()
                .findFragmentById(R.id.fragment_forecast)));
        forecastFragment.setUseTodayLayout(!mTwoPane);

        SunshineSyncAdapter.initializeSyncAdapter(this);

        if (checkPlayServices()) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            boolean sentToken = sp.getBoolean(SENT_TOKEN_TO_SERVER,false);
            sentToken = false;
            if(!sentToken){
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            }
        } else{
            Log.v(LOG_TAG,"PlayServices not available");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onResume() {
        super.onResume();
        String location = Utility.getPreferredLocation( this );
        // update the location in our second pane using the fragment manager
        if (location != null && !location.equals(mLocation)) {
            ForecastFragment ff = (ForecastFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            if (ff != null) {
                ff.onLocationChanged();
            }

            DetailFragment df = (DetailFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if ( null != df ) {
                df.onLocationChanged(location);
            }

            mLocation = location;
        }
    }

    @Override
    public void onItemSelected(Uri contentUri, ForecastAdapter.ForecastAdapterViewHolder vh) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, contentUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(contentUri);

            ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                    new Pair<View,String>(vh.mIconView,getString(R.string.detail_icon_transition_name)));
            ActivityCompat.startActivity(this,intent,activityOptions.toBundle());
        }
    }


    private boolean checkPlayServices(){
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS){
            if(apiAvailability.isUserResolvableError(resultCode)){
                apiAvailability.getErrorDialog(this,resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else{
                Log.i(LOG_TAG,"This device is not supported");
            }
            return false;
        }
        return true;
    }
}