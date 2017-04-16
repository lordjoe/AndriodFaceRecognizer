package com.lordjoe.identifier.threads;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

/**
 *  from https://blog.mindorks.com/threadpoolexecutor-in-android-8e9d22330ee3
 */

public class MainThreadExecutor implements Executor {

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void execute(Runnable runnable) {
        handler.post(runnable);
    }
}