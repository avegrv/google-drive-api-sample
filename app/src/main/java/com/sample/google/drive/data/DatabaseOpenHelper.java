package com.sample.google.drive.data;

import com.sample.google.drive.app.App;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.sample.google.drive.data.DBConstants.*;

class DatabaseOpenHelper extends SQLiteOpenHelper {

    static SQLiteDatabase getAppDatabase() {
        final DatabaseOpenHelper helper = new DatabaseOpenHelper(App.getInstance());
        return helper.getWritableDatabase();
    }

    private DatabaseOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        onCreate(database);
    }
}
