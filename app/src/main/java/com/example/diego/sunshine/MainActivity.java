package com.example.diego.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity implements ForecastFragment.Callback {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private boolean mTwoPane;

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "on-stop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "on-destroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "on-pause");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "on-start");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "on-resume");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.weather_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailFragment())
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
        ForecastFragment fragment = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
        fragment.setUseTodayLayout(!mTwoPane);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else {
            if (id == R.id.action_view_map) {
                showMapLocation();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void showMapLocation() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String location = sharedPref.getString(getString(R.string.preferences_location_key),
                getString(R.string.preferences_location_value));

        Uri uri = Uri.parse("geo:0,0").buildUpon().appendQueryParameter("q", location).build();
        showMap(uri);
    }

    public void showMap(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(LOG_TAG, "Could load implicit action view intent for URI:" + geoLocation.toString());
        }
    }

    @Override
    public void onItemSelected(String date) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putString(ForecastDetail.DATE_KEY, date);

            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(args);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.weather_detail_container, detailFragment)
                    .commit();

        } else {
            Intent intent = new Intent(this, ForecastDetail.class);
            intent.putExtra(ForecastDetail.DATE_KEY, date);
            startActivity(intent);
        }


    }
}
