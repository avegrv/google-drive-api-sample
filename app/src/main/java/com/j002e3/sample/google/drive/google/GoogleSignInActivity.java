package com.j002e3.sample.google.drive.google;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;

import com.j002e3.sample.google.drive.app.AppActivity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

public abstract class GoogleSignInActivity extends AppActivity
        implements GoogleApiClient.OnConnectionFailedListener {

    private static final String LOG_TAG = "GoogleSignIn";

    private static final int GOOGLE_SIGN_IN_REQUEST = 1010;

    private GoogleApiClient googleApiClient;

    @Override
    protected void onStart() {
        super.onStart();
        initGoogleApiClient();
    }

    @Override
    protected void onStop() {
        super.onStop();
        destroyGoogleApiClient();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_SIGN_IN_REQUEST) {
            final GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            boolean isGoogleLoginSuccess = result.isSuccess();
            Log.i(LOG_TAG, String.format("onActivityResult success=%b", isGoogleLoginSuccess));
            if (isGoogleLoginSuccess) {
                onGoogleSignedInSuccess();
            } else {
                onGoogleSignedInFailed();
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull final ConnectionResult connectionResult) {
        onGoogleSignedInFailed();
    }

    protected void startGoogleSignIn() {
        Log.i(LOG_TAG, "GoogleSignIn: starting Google login");
        final OptionalPendingResult<GoogleSignInResult> optionalPendingResult
                = Auth.GoogleSignInApi.silentSignIn(googleApiClient);
        optionalPendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
            @Override
            public void onResult(@NonNull final GoogleSignInResult googleSignInResult) {
                if (!googleSignInResult.isSuccess()) {
                    final Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                    startActivityForResult(intent, GOOGLE_SIGN_IN_REQUEST);
                } else {
                    onGoogleSignedInSuccess();
                }
            }
        });
    }

    protected abstract GoogleSignInOptions getGoogleSignInOptions();

    protected abstract void onGoogleSignedInSuccess();

    protected abstract void onGoogleSignedInFailed();

    private void initGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, getGoogleSignInOptions())
                .build();
    }

    private void destroyGoogleApiClient() {
        if (googleApiClient != null) {
            googleApiClient.stopAutoManage(this);
            googleApiClient.disconnect();
        }
    }
}
