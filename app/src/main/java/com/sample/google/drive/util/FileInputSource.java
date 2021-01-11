package com.sample.google.drive.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public final class FileInputSource implements InputSource {

    private final File file;

    public FileInputSource(File file) {
        this.file = file;
    }

    @Override
    public InputStream open() throws FileNotFoundException {
        return new FileInputStream(file);
    }

    @Override
    public long length() {
        return file.length();
    }
}
