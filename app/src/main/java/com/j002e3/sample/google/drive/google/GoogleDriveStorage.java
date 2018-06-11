package com.j002e3.sample.google.drive.google;

import android.support.annotation.NonNull;

import java.io.File;

public interface GoogleDriveStorage {

    void uploadFile(@NonNull final File file, @NonNull final String name, @NonNull Callback<Void> callback);

    void downloadFile(@NonNull final File file, @NonNull final String name, @NonNull Callback<Void> callback);
}
