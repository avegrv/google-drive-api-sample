package com.j002e3.sample.google.drive.google;

import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import com.j002e3.sample.google.drive.util.ByteSegments;
import com.j002e3.sample.google.drive.util.FileInputSource;

import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;

class GoogleDriveApiDataRepository implements GoogleDriveStorage {

    private static final String LOG_TAG = "GoogleDriveApi";

    private GoogleDriveApiProvider provider;

    GoogleDriveApiDataRepository(GoogleDriveApiProvider provider) {
        this.provider = provider;
    }

    @Override
    public void uploadFile(
            @NonNull final File file,
            @NonNull final String name,
            @NonNull final Callback<Void> callback
    ) {
        final Callback<Void> createCallback = new Callback<Void>() {
            @Override
            public void onSuccess(final Void block) {
                writeFile(file, name, callback);
            }

            @Override
            public void onError(@NonNull final Exception e) {
                callback.onError(e);
            }
        };
        createFile(name, createCallback);
    }

    @Override
    public void downloadFile(
            @NonNull final File file,
            @NonNull final String name,
            @NonNull final Callback<Void> callback
    ) {
        final Callback<DriveId> readCallback = new Callback<DriveId>() {
            @Override
            public void onSuccess(final DriveId block) {
                final DriveFile driveFile = block.asDriveFile();
                Task<DriveContents> openFileTask =
                        provider.getDriveResourceClient().openFile(driveFile, DriveFile.MODE_READ_ONLY);
                openFileTask
                        .continueWithTask(new Continuation<DriveContents, Task<Void>>() {

                            @Override
                            public Task<Void> then(@NonNull final Task<DriveContents> task) throws Exception {
                                final DriveContents contents = task.getResult();
                                final byte[] encoded = ByteSegments.toByteArray(contents.getInputStream());
                                final byte[] decoded = Base64.decode(encoded, Base64.DEFAULT);
                                try (FileOutputStream stream = new FileOutputStream(file)) {
                                    stream.write(decoded);
                                }
                                return provider.getDriveResourceClient().discardContents(contents);
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<Void>() {

                            @Override
                            public void onSuccess(final Void empty) {
                                callback.onSuccess(null);
                                Log.i(LOG_TAG, "download contents success");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {

                            @Override
                            public void onFailure(@NonNull final Exception e) {
                                callback.onError(e);
                            }
                        });
            }

            @Override
            public void onError(@NonNull final Exception e) {
                callback.onError(e);
            }
        };
        readFileMetaData(name, readCallback);
    }

    private void writeFile(
            @NonNull final File file,
            @NonNull final String name,
            @NonNull final Callback<Void> callback
    ) {
        final Callback<DriveId> reatrieveCallback = new Callback<DriveId>() {
            @Override
            public void onSuccess(final DriveId block) {
                final DriveFile driveFile = block.asDriveFile();
                Task<DriveContents> openFileTask =
                        provider.getDriveResourceClient().openFile(driveFile, DriveFile.MODE_WRITE_ONLY);
                openFileTask.continueWith
                        (new Continuation<DriveContents, Task<Void>>() {
                            @Override
                            public Task<Void> then(@NonNull final Task<DriveContents> task) throws Exception {
                                final DriveContents contents = task.getResult();
                                byte[] bytes = ByteSegments.toByteArray(new FileInputSource(file));
                                final String encoded = Base64.encodeToString(bytes, Base64.DEFAULT);
                                final OutputStream outputStream = contents.getOutputStream();
                                try (Writer writer = new OutputStreamWriter(outputStream)) {
                                    writer.write(encoded);
                                }
                                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                        .setStarred(true)
                                        .setLastViewedByMeDate(new Date())
                                        .build();
                                Task<Void> commitTask =
                                        provider.getDriveResourceClient().commitContents(contents, changeSet);
                                return commitTask;
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<Task<Void>>() {
                            @Override
                            public void onSuccess(final Task<Void> voidTask) {
                                callback.onSuccess(null);
                                Log.i(LOG_TAG, "write file success");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {

                            @Override
                            public void onFailure(@NonNull final Exception e) {
                                callback.onError(e);
                                Log.e(LOG_TAG, "write file error", e);
                            }
                        });
            }

            @Override
            public void onError(@NonNull final Exception e) {
                callback.onError(e);
                Log.e(LOG_TAG, "Unable to retrieve contents", e);
            }
        };
        readFileMetaData(name, reatrieveCallback);
    }

    private void readFileMetaData(
            @NonNull final String name,
            @NonNull final Callback<DriveId> callback
    ) {
        final Task<DriveFolder> appFolderTask = provider.getDriveResourceClient().getAppFolder();
        appFolderTask
                .addOnSuccessListener(new OnSuccessListener<DriveFolder>() {

                    @Override
                    public void onSuccess(final DriveFolder driveFolder) {
                        retrieveMetaData(driveFolder, name, callback);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {

                    @Override
                    public void onFailure(@NonNull final Exception e) {
                        callback.onError(e);
                    }
                });
    }

    private void retrieveMetaData(
            @NonNull DriveFolder driveFolder,
            @NonNull final String fileName,
            @NonNull final Callback<DriveId> callback
    ) {
        Query query = new Query.Builder()
                .addFilter(Filters.contains(SearchableField.TITLE, fileName))
                .build();
        Task<MetadataBuffer> queryTask = provider.getDriveResourceClient().queryChildren(driveFolder, query);
        queryTask.addOnSuccessListener(new OnSuccessListener<MetadataBuffer>() {

            @Override
            public void onSuccess(final MetadataBuffer metadata) {
                try {
                    for (Metadata md : metadata) {
                        if (md == null || !md.isDataValid() || md.isTrashed()) {
                            continue;
                        }
                        // collect files
                        DriveId driveId = md.getDriveId();
                        String name = md.getTitle();
                        if (name.equals(fileName)) {
                            callback.onSuccess(driveId);
                            return;
                        }
                    }
                    Log.e(LOG_TAG, "Unable to find file");
                } finally {
                    metadata.release();
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {

                    @Override
                    public void onFailure(@NonNull final Exception e) {
                        callback.onError(e);
                    }
                });
    }

    private void createFile(
            @NonNull final String fileName,
            @NonNull final Callback<Void> callback
    ) {
        final Task<DriveFolder> appFolderTask = provider.getDriveResourceClient().getAppFolder();
        final Task<DriveContents> createContentsTask = provider.getDriveResourceClient().createContents();
        Tasks.whenAll(appFolderTask, createContentsTask)
                .continueWithTask(new Continuation<Void, Task<DriveFile>>() {
                    @Override
                    public Task<DriveFile> then(@NonNull final Task<Void> task) throws Exception {
                        DriveFolder parent = appFolderTask.getResult();
                        DriveContents contents = createContentsTask.getResult();
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle(fileName)
                                .setMimeType("text/plain")
                                .setStarred(true)
                                .build();
                        return provider.getDriveResourceClient().createFile(parent, changeSet, contents);
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<DriveFile>() {

                    @Override
                    public void onSuccess(final DriveFile driveFile) {
                        callback.onSuccess(null);
                        Log.i(LOG_TAG, "Create file success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {

                    @Override
                    public void onFailure(@NonNull final Exception e) {
                        callback.onError(e);
                    }
                });
    }
}
