package com.j002e3.sample.google.drive.google;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveResourceClient;

import com.j002e3.sample.google.drive.R;

import android.support.annotation.CallSuper;

public abstract class GoogleDriveActivity extends GoogleSignInActivity implements GoogleDriveApiProvider {

    private DriveClient driveClient;
    private DriveResourceClient driveResourceClient;
    private GoogleDriveStorage storage;

    @Override
    protected void onStart() {
        super.onStart();
        startGoogleSignIn();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected GoogleSignInOptions getGoogleSignInOptions() {
        return new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Drive.SCOPE_FILE, Drive.SCOPE_APPFOLDER)
                .build();
    }

    @Override
    protected void onGoogleSignedInSuccess() {
        initializeDriveClient(GoogleSignIn.getLastSignedInAccount(this));
    }

    @Override
    protected void onGoogleSignedInFailed() {
        showMessage(R.string.message_google_sign_in_failed);
    }

    private void initializeDriveClient(GoogleSignInAccount signInAccount) {
        driveClient = Drive.getDriveClient(getApplicationContext(), signInAccount);
        driveResourceClient = Drive.getDriveResourceClient(getApplicationContext(), signInAccount);
        onDriveClientReady();
    }

    @CallSuper
    protected void onDriveClientReady() {
        storage = GoogleDriveStorageFactory.createGoogleDriveStorage(this);
    }

    public DriveClient getDriveClient() {
        return driveClient;
    }

    public DriveResourceClient getDriveResourceClient() {
        return driveResourceClient;
    }

    public GoogleDriveStorage getStorage() {
        return storage;
    }
}
