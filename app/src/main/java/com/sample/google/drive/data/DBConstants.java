package com.sample.google.drive.data;

public class DBConstants {

    public static final String DB_LOCATION = "/data/data/com.sample.google.drive/databases/" + DBConstants.DB_NAME;

    static final String DB_NAME = "GoogleDriveSampleBd";
    static final int DB_VERSION = 1;

    static final String TABLE_INFO = "TABLE_INFO";
    static final String INFO_FIELD_TEXT = "INFO_FIELD_TEXT";

    static final String DATABASE_CREATE;
    static {
        DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS "
                + TABLE_INFO
                + " (" + INFO_FIELD_TEXT + " VARCHAR);";
    }
}
