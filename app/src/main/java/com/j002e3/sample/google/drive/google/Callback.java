package com.j002e3.sample.google.drive.google;

import android.support.annotation.NonNull;

public interface Callback<T> {

    void onSuccess(T block);

    void onError(@NonNull final Exception e);
}
