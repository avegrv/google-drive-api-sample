package com.j002e3.sample.google.drive.google;

import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveResourceClient;

interface GoogleDriveApiProvider {

    DriveClient getDriveClient();

    DriveResourceClient getDriveResourceClient();
}
