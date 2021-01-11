package com.sample.google.drive.app;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

public abstract class AppActivity extends AppCompatActivity {

    protected final void showMessage(@NonNull String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    protected final void showMessage(@StringRes int res) {
        showMessage(getString(res));
    }
}
