package com.j002e3.sample.google.drive.app;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public abstract class AppActivity extends AppCompatActivity {

    protected final void showMessage(@NonNull String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    protected final void showMessage(@StringRes int res) {
        showMessage(getString(res));
    }
}
