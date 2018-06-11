package com.j002e3.sample.google.drive.google;

public class GoogleDriveStorageFactory {

    public static GoogleDriveStorage createGoogleDriveStorage(GoogleDriveApiProvider provider) {
        return new GoogleDriveApiDataRepository(provider);
    }
}
