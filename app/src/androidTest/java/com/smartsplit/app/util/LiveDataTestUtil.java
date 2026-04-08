package com.smartsplit.app.util;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public final class LiveDataTestUtil {

    private LiveDataTestUtil() {
        // Utility class.
    }

    public static <T> T getOrAwaitValue(LiveData<T> liveData) throws InterruptedException, TimeoutException {
        return getOrAwaitValue(liveData, 5, TimeUnit.SECONDS);
    }

    public static <T> T getOrAwaitValue(LiveData<T> liveData, long timeout, TimeUnit unit)
        throws InterruptedException, TimeoutException {
        AtomicReference<T> valueRef = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Observer<T> observer = new Observer<T>() {
            @Override
            public void onChanged(T value) {
                valueRef.set(value);
                latch.countDown();
                liveData.removeObserver(this);
            }
        };

        liveData.observeForever(observer);

        if (!latch.await(timeout, unit)) {
            liveData.removeObserver(observer);
            throw new TimeoutException("LiveData value was never set.");
        }

        return valueRef.get();
    }
}
