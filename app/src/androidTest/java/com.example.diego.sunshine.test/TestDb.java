package com.example.diego.sunshine.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.example.diego.sunshine.data.WeatherContract.LocationEntry;
import com.example.diego.sunshine.data.WeatherContract.WeatherEntry;
import com.example.diego.sunshine.data.WeatherDbHelper;

import java.util.Map;
import java.util.Set;

/**
 * Created by diego on 21/11/14.
 */
public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb() {

        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Long rowId = testInsertRead(LocationEntry.TABLE_NAME, getLocationValues(), db);
        testInsertRead(WeatherEntry.TABLE_NAME, getWeatherValues(rowId), db);

        dbHelper.close();
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

    public Long testInsertRead(String tableName, ContentValues values, SQLiteDatabase db) {
        long locationRowId;
        locationRowId = db.insert(tableName, null, values);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        String[] key_string = new String[values.size()];
        Set<Map.Entry<String, Object>> keys = values.valueSet();

        int i = 0;
        for (Map.Entry<String, Object> key : keys) {
            key_string[i++] = key.getKey();
        }

        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
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
}