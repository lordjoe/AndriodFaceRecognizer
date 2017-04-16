package com.lordjoe.identifier.threads;

import java.util.concurrent.ThreadFactory;

/**
 * copied from https://blog.mindorks.com/threadpoolexecutor-in-android-8e9d22330ee3
 */

public class PriorityThreadFactory implements ThreadFactory {

    private final int mThreadPriority;

    public PriorityThreadFactory(int threadPriority) {
        mThreadPriority = threadPriority;
    }

    @Override
    public Thread newThread(final Runnable runnable) {
        Runnable wrapperRunnable = new Runnable() {
            @Override
            public void run() {
//                try {
//                      Process.setThreadPriority(mThreadPriority);
//                } catch (Throwable t) {
//
//                }
                runnable.run();
            }
        };
        return new Thread(wrapperRunnable);
    }

}