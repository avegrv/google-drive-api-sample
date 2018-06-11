package com.j002e3.sample.google.drive.util;

import android.support.annotation.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public final class IOUtils {

    public static void skip(InputStream is, long n) throws IOException {
        while (n > 0) {
            long skipped = is.skip(n);
            if (skipped > 0) {
                n -= skipped;
            } else if (skipped == 0) {
                if (is.read() == -1) {
                    break;
                } else {
                    --n;
                }
            }
        }
    }

    public static void close(@Nullable Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }
}
