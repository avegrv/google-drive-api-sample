package com.sample.google.drive.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class InfoRepository {

    private final SQLiteDatabase db = DatabaseOpenHelper.getAppDatabase();

    public void writeInfo(@NonNull String info) {
        ContentValues values = new ContentValues();
        values.put(DBConstants.INFO_FIELD_TEXT, info);
        db.insert(DBConstants.TABLE_INFO, null, values);
        db.close();
    }

    @Nullable
    public String getInfo() {
        String info;
        final String[] cols = new String[]{DBConstants.INFO_FIELD_TEXT};
        try (Cursor cursor = db.query(
                true,
                DBConstants.TABLE_INFO,
                cols,
                null,
                null,
                null,
                null,
                null,
                null)) {
            cursor.moveToLast();
            info = cursor.getString(0);
        }
        return info;
    }
}
