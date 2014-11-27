package com.example.diego.sunshine.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.example.diego.sunshine.data.WeatherContract.LocationEntry;
import com.example.diego.sunshine.data.WeatherContract.WeatherEntry;

/**
 * Created by diego on 11/24/2014.
 */
public class WeatherProvider extends ContentProvider {

    private static final int WEATHER = 100;
    private static final int WEATHER_WITH_LOCATION = 101;
    private static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    private static final int LOCATION = 300;
    private static final int LOCATION_ID = 301;

    private static final UriMatcher  sUriMatcher = buildUriMatcher();
    private static final SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder;

    static {
        sWeatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();
        sWeatherByLocationSettingQueryBuilder.setTables(WeatherEntry.TABLE_NAME +
                        " INNER JOIN " + LocationEntry.TABLE_NAME + " ON " +
                        getTableColumnString(WeatherEntry.TABLE_NAME, WeatherEntry.COLUMN_LOC_KEY)
                        + " = " +
                        getTableColumnString(LocationEntry.TABLE_NAME, LocationEntry._ID)


        );
    }

    private static final String sLocationSettingSelection =
            getTableColumnStringForQuery(LocationEntry.TABLE_NAME, LocationEntry.COLUMN_LOCATION_SETTING, " = ");
    private static final String sLocationSettingWithStartDateSelection =
            getTableColumnStringForQuery(LocationEntry.TABLE_NAME, LocationEntry.COLUMN_LOCATION_SETTING, " = ") +
                    " AND " +
                    WeatherContract.WeatherEntry.COLUMN_DATETEXT + " >= ? ";
    private static final String sLocationSettingWithDateSelection =
            getTableColumnStringForQuery(LocationEntry.TABLE_NAME, LocationEntry.COLUMN_LOCATION_SETTING, " = ") +
                    " AND " +
                    getTableColumnStringForQuery(WeatherEntry.TABLE_NAME, WeatherEntry.COLUMN_DATETEXT, " = ");
    private static WeatherDbHelper mOpenHelper;

    private static String getTableColumnString(String tableName, String columnName) {
        return tableName + "." + columnName;
    }

    private static String getTableColumnStringForQuery(String tableName, String columnName, String comparator) {
        return tableName + "." + columnName + " " + comparator + " " + " ? ";
    }

    private static UriMatcher buildUriMatcher(){

        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_WEATHER, WEATHER );
        matcher.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_WEATHER+"/*", WEATHER_WITH_LOCATION );
        matcher.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_WEATHER+"/*/*", WEATHER_WITH_LOCATION_AND_DATE );
        matcher.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_LOCATION, LOCATION );
        matcher.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_LOCATION+"/#", LOCATION_ID);
        return matcher;
    }


    @Override
    public boolean onCreate() {
        mOpenHelper = new WeatherDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "weather/*/*"
            case WEATHER_WITH_LOCATION_AND_DATE:
            {
                retCursor = getWeatherByLocationSettingAndDate(uri, projection, sortOrder);
                break;
            }
            // "weather/*"
            case WEATHER_WITH_LOCATION: {
                retCursor = getWeatherByLocationSetting(uri, projection, sortOrder);
                break;
            }
            // "weather"
            case WEATHER: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "location/*"
            case LOCATION_ID: {
                Long id = ContentUris.parseId(uri);
                selectionArgs = new String[1];
                selectionArgs[0] = id.toString();
                retCursor = mOpenHelper.getReadableDatabase().query(
                        WeatherContract.LocationEntry.TABLE_NAME,
                        projection,
                        WeatherContract.LocationEntry._ID +" = ? ",
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "location"
            case LOCATION: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        WeatherContract.LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case  WEATHER_WITH_LOCATION_AND_DATE:
                return WeatherContract.WeatherEntry.CONTENT_ITEM_TYPE;
            case WEATHER_WITH_LOCATION:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case WEATHER:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case LOCATION:
                return WeatherContract.LocationEntry.CONTENT_TYPE;
            case LOCATION_ID:
                return WeatherContract.LocationEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown URI "+uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case WEATHER: {
                long _id = db.insert(WeatherEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = WeatherEntry.buildWeatherUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case LOCATION: {
                long _id = db.insert(LocationEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = LocationEntry.buildLocationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int nRowsAffected = 0;

        switch (match) {
            case WEATHER: {
                nRowsAffected = db.delete(WeatherEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case LOCATION: {
                nRowsAffected = db.delete(LocationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return nRowsAffected;

    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        return 0;
    }


    private Cursor getWeatherByLocationSetting(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        String startDate = WeatherContract.WeatherEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == null) {
            selection = sLocationSettingSelection;
            selectionArgs = new String[]{locationSetting};
        } else {
            selectionArgs = new String[]{locationSetting, startDate};
            selection = sLocationSettingWithStartDateSelection;
        }

        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }


    private Cursor getWeatherByLocationSettingAndDate(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        String startDate = WeatherContract.WeatherEntry.getDateFromUri(uri);

        String[] selectionArgs = new String[]{locationSetting, startDate};
        String selection = sLocationSettingWithDateSelection;

        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

}
