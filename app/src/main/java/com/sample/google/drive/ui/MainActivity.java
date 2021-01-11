package com.sample.google.drive.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.api.ApiException;
import com.google.api.services.drive.Drive;
import com.sample.google.drive.R;
import com.sample.google.drive.data.DBConstants;
import com.sample.google.drive.data.InfoRepository;
import com.sample.google.drive.google.GoogleDriveActivity;
import com.sample.google.drive.google.GoogleDriveApiDataRepository;

import java.io.File;

import androidx.constraintlayout.widget.Group;

public class MainActivity extends GoogleDriveActivity {

    private static final String LOG_TAG = "MainActivity";

    private static final String GOOGLE_DRIVE_DB_LOCATION = "db";

    private Button googleSignIn;
    private Group contentViews;
    private EditText inputToDb;
    private Button writeToDb;
    private Button saveToGoogleDrive;
    private Button restoreFromDb;

    private GoogleDriveApiDataRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        initViews();
    }

    @Override
    protected void onGoogleDriveSignedInSuccess(Drive driveApi) {
        showMessage(R.string.message_drive_client_is_ready);
        repository = new GoogleDriveApiDataRepository(driveApi);
        googleSignIn.setVisibility(View.GONE);
        contentViews.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onGoogleDriveSignedInFailed(ApiException exception) {
        showMessage(R.string.message_google_sign_in_failed);
        Log.e(LOG_TAG, "error google drive sign in", exception);
    }

    private void findViews() {
        googleSignIn = findViewById(R.id.google_sign_in);
        contentViews = findViewById(R.id.content_views);
        inputToDb = findViewById(R.id.edit_text_db_input);
        writeToDb = findViewById(R.id.write_to_db);
        saveToGoogleDrive = findViewById(R.id.save_to_google_drive);
        restoreFromDb = findViewById(R.id.restore_from_db);
    }

    private void initViews() {
        googleSignIn.setOnClickListener(v -> {
            startGoogleDriveSignIn();
        });

        writeToDb.setOnClickListener(v -> {
            String text = inputToDb.getText().toString();
            InfoRepository repository = new InfoRepository();
            repository.writeInfo(text);
        });

        saveToGoogleDrive.setOnClickListener(v -> {
            File db = new File(DBConstants.DB_LOCATION);
            if (repository == null) {
                showMessage(R.string.message_google_sign_in_failed);
                return;
            }

            repository.uploadFile(db, GOOGLE_DRIVE_DB_LOCATION)
                    .addOnSuccessListener(r -> showMessage("Upload success"))
                    .addOnFailureListener(e -> {
                        Log.e(LOG_TAG, "error upload file", e);
                        showMessage("Error upload");
                    });
        });

        restoreFromDb.setOnClickListener(v -> {
            if (repository == null) {
                showMessage(R.string.message_google_sign_in_failed);
                return;
            }

            File db = new File(DBConstants.DB_LOCATION);
            db.getParentFile().mkdirs();
            db.delete();
            repository.downloadFile(db, GOOGLE_DRIVE_DB_LOCATION)
                    .addOnSuccessListener(r -> {
                        InfoRepository repository = new InfoRepository();
                        String infoText = repository.getInfo();
                        inputToDb.setText(infoText);
                        showMessage("Retrieved");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(LOG_TAG, "error download file", e);
                        showMessage("Error download");
                    });
        });
    }
}
