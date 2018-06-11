package com.j002e3.sample.google.drive.ui;

import com.j002e3.sample.google.drive.R;
import com.j002e3.sample.google.drive.data.DBConstants;
import com.j002e3.sample.google.drive.data.InfoRepository;
import com.j002e3.sample.google.drive.google.Callback;
import com.j002e3.sample.google.drive.google.GoogleDriveActivity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;

public class MainActivity extends GoogleDriveActivity {

    private static final String GOOGLE_DRIVE_DB_LOCATION = "db";

    private EditText inputToDb;

    private Button writeToDb;

    private Button saveToGoogleDrive;

    private Button restoreFromDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        initViews();
    }

    @Override
    protected void onDriveClientReady() {
        super.onDriveClientReady();
        showMessage(R.string.message_drive_client_is_ready);
    }

    private void findViews() {
        inputToDb = findViewById(R.id.edit_text_db_input);
        writeToDb = findViewById(R.id.write_to_db);
        saveToGoogleDrive = findViewById(R.id.save_to_google_drive);
        restoreFromDb = findViewById(R.id.restore_from_db);
    }

    private void initViews() {
        writeToDb.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                String text = inputToDb.getText().toString();
                InfoRepository repository = new InfoRepository();
                repository.writeInfo(text);
            }
        });
        saveToGoogleDrive.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                File db = new File(DBConstants.DB_LOCATION);
                final Callback<Void> callback = new Callback<Void>() {
                    @Override
                    public void onSuccess(final Void block) {
                        showMessage("Upload success");
                    }

                    @Override
                    public void onError(@NonNull final Exception e) {
                        showMessage("Error upload");
                    }
                };
                getStorage().uploadFile(db, GOOGLE_DRIVE_DB_LOCATION, callback);
            }
        });
        restoreFromDb.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                final Callback<Void> callback = new Callback<Void>() {
                    @Override
                    public void onSuccess(final Void block) {
                        InfoRepository repository = new InfoRepository();
                        String infoText = repository.getInfo();
                        inputToDb.setText(infoText);
                        showMessage("Retrieved");
                    }

                    @Override
                    public void onError(@NonNull final Exception e) {
                        showMessage("Unable to retrieve contents");
                    }
                };
                File db = new File(DBConstants.DB_LOCATION);
                db.getParentFile().mkdirs();
                db.delete();
                getStorage().downloadFile(db, GOOGLE_DRIVE_DB_LOCATION, callback);
            }
        });
    }
}
