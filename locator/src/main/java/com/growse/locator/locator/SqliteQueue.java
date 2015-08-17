package com.growse.locator.locator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

/**
 * Created by andrew on 17/08/2015.
 */
public class SqliteQueue implements Queue<String> {
    private LocationSqliteOpenHelper helper;

    public SqliteQueue(Context context) {
        helper = new LocationSqliteOpenHelper(context);
    }

    @Override
    public int size() {
        Cursor cursor = helper.getReadableDatabase().rawQuery("select count(*) from locations", null);
        cursor.moveToFirst();
        int size = cursor.getInt(0);
        cursor.close();
        return size;
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @NonNull
    @Override
    public <T> T[] toArray(T[] ts) {
        return null;
    }

    @Override
    public boolean add(String s) {
        try {
            helper.getWritableDatabase().execSQL("insert into locations (LOCATION) values (?)", new String[]{s});
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean addAll(Collection<? extends String> collection) {
        return false;
    }

    @Override
    public void clear() {
        helper.getWritableDatabase().execSQL("delete from locations");
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @NonNull
    @Override
    public Iterator<String> iterator() {
        return null;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return false;
    }

    @Override
    public boolean offer(String s) {
        return false;
    }

    @Override
    public String remove() {
        return null;
    }

    @Override
    public String poll() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        Cursor cursor = db.query("locations", new String[]{"ID", "LOCATION"}, null, null, null, null, "ID asc", "1");

        cursor.moveToFirst();
        if (cursor.getCount() == 0) {
            return null;
        }
        int id = cursor.getInt(0);
        String result = cursor.getString(1);
        cursor.close();
        db.delete("locations", "id=?", new String[]{String.valueOf(id)});
        db.setTransactionSuccessful();
        db.endTransaction();
        return result;
    }

    @Override
    public String element() {
        return null;
    }

    @Override
    public String peek() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        Cursor cursor = db.query(false, "locations", new String[]{"ID", "LOCATION"}, null, null, null, null, "ID", "1");
        cursor.moveToFirst();
        String result = cursor.getString(1);
        cursor.close();
        db.endTransaction();
        return result;
    }
}

class LocationSqliteOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String LOCATION_TABLE_NAME = "locations";
    private static final String DATABASE_NAME = "growse_locations.db";
    private static final String LOCATION_TABLE_CREATE =
            "CREATE TABLE " + LOCATION_TABLE_NAME + " (" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "LOCATION TEXT);";

    LocationSqliteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LOCATION_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // TODO: should probably do something here.
    }
}
