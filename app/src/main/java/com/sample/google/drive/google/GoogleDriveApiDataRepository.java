package com.sample.google.drive.google;

import android.util.Base64;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.sample.google.drive.util.ByteSegments;
import com.sample.google.drive.util.FileInputSource;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;

public class GoogleDriveApiDataRepository {

    private final String FILE_MIME_TYPE = "text/plain";
    private final String APP_DATA_FOLDER_SPACE = "appDataFolder";

    private final Executor mExecutor = Executors.newSingleThreadExecutor();

    private final Drive mDriveService;

    public GoogleDriveApiDataRepository(Drive driveApi) {
        this.mDriveService = driveApi;
    }

    public Task<Void> uploadFile(@NonNull final java.io.File file, @NonNull final String fileName) {
        return createFile(fileName)
                .continueWithTask(mExecutor, task -> {
                    final String fileId = task.getResult();
                    if (fileId == null) {
                        throw new IOException("Null file id when requesting file upload.");
                    }
                    return writeFile(file, fileId, fileName);
                });
    }

    public Task<Void> downloadFile(@NonNull final java.io.File file, @NonNull final String fileName) {
        return queryFiles()
                .continueWithTask(mExecutor, task -> {
                    final FileList fileList = task.getResult();
                    if (fileList == null) {
                        throw new IOException("Null file list when requesting file download.");
                    }
                    File currentFile = null;
                    for (File f : fileList.getFiles()) {
                        if (f.getName().equals(fileName)) {
                            currentFile = f;
                            break;
                        }
                    }
                    if (currentFile == null) {
                        throw new IOException("File not found when requesting file download.");
                    }

                    final String fileId = currentFile.getId();
                    return readFile(file, fileId);
                });
    }

    private Task<Void> readFile(
            @NonNull final java.io.File file,
            @NonNull final String fileId
    ) {
        return Tasks.call(mExecutor, () -> {
            String encoded;
            try (InputStream is = mDriveService.files().get(fileId).executeMediaAsInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                encoded = stringBuilder.toString();
            }

            final byte[] decoded = Base64.decode(encoded, Base64.DEFAULT);
            try (FileOutputStream stream = new FileOutputStream(file)) {
                stream.write(decoded);
            }
            return null;
        });
    }

    public Task<FileList> queryFiles() {
        return Tasks.call(mExecutor, () -> mDriveService.files().list().setSpaces(APP_DATA_FOLDER_SPACE).execute());
    }

    private Task<Void> writeFile(
            @NonNull final java.io.File file,
            @NonNull final String fileId,
            @NonNull final String fileName
    ) {
        return Tasks.call(mExecutor, () -> {
            File metadata = getMetaData(fileName);

            byte[] bytes = ByteSegments.toByteArray(new FileInputSource(file));
            final String encoded = Base64.encodeToString(bytes, Base64.DEFAULT);
            ByteArrayContent contentStream = ByteArrayContent.fromString(FILE_MIME_TYPE, encoded);

            // Update the metadata and contents.
            mDriveService.files().update(fileId, metadata, contentStream).execute();
            return null;
        });
    }

    private Task<String> createFile(@NonNull final String fileName) {
        return Tasks.call(mExecutor, () -> {
            File metadata = getMetaData(fileName);
            metadata.setParents(Collections.singletonList(APP_DATA_FOLDER_SPACE));
            File googleFile = mDriveService.files().create(metadata).execute();
            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }
            return googleFile.getId();
        });
    }

    private File getMetaData(@NonNull final String fileName) {
        return new File()
                .setMimeType(FILE_MIME_TYPE)
                .setName(fileName);
    }
}
