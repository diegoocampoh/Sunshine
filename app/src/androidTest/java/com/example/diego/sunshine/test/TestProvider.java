package com.example.diego.sunshine.test;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;

import com.example.diego.sunshine.data.WeatherContract.LocationEntry;
import com.example.diego.sunshine.data.WeatherContract.WeatherEntry;
import com.example.diego.sunshine.data.WeatherDbHelper;

/**
 * Created by diego on 21/11/14.
 */
public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    static public String TEST_DATE = "20141205";
    static public String TEST_LOCATION = "99705";


    public void testDeleteDb() throws Throwable {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }


    public void testGetType() {
        // content://com.example.android.sunshine.app/weather/
        String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testLocation = "94074";
        // content://com.example.android.sunshine.app/weather/94074
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocation(testLocation));
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testDate = "20140612";
        // content://com.example.android.sunshine.app/weather/94074/20140612
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocationWithDate(testLocation, testDate));
        // vnd.android.cursor.item/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_ITEM_TYPE, type);

        // content://com.example.android.sunshine.app/location/
        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/location
        assertEquals(LocationEntry.CONTENT_TYPE, type);

        // content://com.example.android.sunshine.app/location/1
        type = mContext.getContentResolver().getType(LocationEntry.buildLocationUri(1L));
        // vnd.android.cursor.item/com.example.android.sunshine.app/location
        assertEquals(LocationEntry.CONTENT_ITEM_TYPE, type);
    }

    public void testInsertReadProvider() {

        ContentValues testValues = getLocationValues();

        Uri locationUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, testValues);
        long locationRowId = ContentUris.parseId(locationUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDb.validateCursor(cursor, testValues);

        // Now see if we can successfully query if we include the row id
        cursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDb.validateCursor(cursor, testValues);

        // Fantastic.  Now that we have a location, add some weather!
        ContentValues weatherValues = getWeatherValues(locationRowId);

        Uri weatherInsertUri = mContext.getContentResolver()
                .insert(WeatherEntry.CONTENT_URI, weatherValues);
        assertTrue(weatherInsertUri != null);

        // A cursor is your primary interface to the query results.
        Cursor weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        TestDb.validateCursor(weatherCursor, weatherValues);


        // Add the location values in with the weather data so that we can make
        // sure that the join worked and we actually get all the values back
        addAllContentValues(weatherValues, testValues);

        // Get the joined Weather and Location data
        weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocation(TEST_LOCATION),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestDb.validateCursor(weatherCursor, weatherValues);

        // Get the joined Weather and Location data with a start date
        weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocationWithStartDate(
                        TEST_LOCATION, TEST_DATE),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestDb.validateCursor(weatherCursor, weatherValues);

        // Get the joined Weather data for a specific date
        weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocationWithDate(TEST_LOCATION, TEST_DATE),
                null,
                null,
                null,
                null
        );
        TestDb.validateCursor(weatherCursor, weatherValues);
    }

    private ContentValues getLocationValues() {
        // Test data we're going to insert into the DB to see if it works.
        String testLocationSetting = "99705";
        String testCityName = "North Pole";
        double testLatitude = 64.7488;
        double testLongitude = -147.353;
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(LocationEntry.COLUMN_LOCATION_SETTING, testLocationSetting);
        values.put(LocationEntry.COLUMN_CITY_NAME, testCityName);
        values.put(LocationEntry.COLUMN_LAT, testLatitude);
        values.put(LocationEntry.COLUMN_LON, testLongitude);
        return values;
    }

    private ContentValues getWeatherValues(Long locationRowId) {
        // Fantastic.  Now that we have a location, add some weather!
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(WeatherEntry.COLUMN_DATETEXT, "20141205");
        weatherValues.put(WeatherEntry.COLUMN_DEGREES, 1.1);
        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.2);
        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.3);
        weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, 75);
        weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, 65);
        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
        weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 5.5);
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 321);
        return weatherValues;
    }


    /*public Long testInsertRead(String tableName, ContentValues values) {
        long locationRowId;

        locationRowId = mContext.getContentResolver().insert(tableName, null, values);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        String[] key_string = new String[values.size()];
        Set<Map.Entry<String, Object>> keys = values.valueSet();

        int i = 0;
        for (Map.Entry<String, Object> key : keys) {
            key_string[i++] = key.getKey();
        }

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                tableName,  // Table to Query
                key_string,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // If possible, move to the first row of the query results.
        if (cursor.moveToFirst()) {
            // Get the value in each column by finding the appropriate column index.

            for (String column : key_string) {
                String dbValue = cursor.getString(cursor.getColumnIndex(column));
                assertEquals(dbValue, values.get(column).toString());
            }

        } else {
            // That's weird, it works on MY machine...
            fail("No values returned :(");
        }

        cursor.close();
        return locationRowId;
    }
    */
    // The target api annotation is needed for the call to keySet -- we wouldn't want
    // to use this in our app, but in a test it's fine to assume a higher target.
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    void addAllContentValues(ContentValues destination, ContentValues source) {
        for (String key : source.keySet()) {
            destination.put(key, source.getAsString(key));
        }
    }

}