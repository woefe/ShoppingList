package com.woefe.shoppinglist;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppExecutors {
    private final Executor diskIO;
    private final Executor netIO;

    private AppExecutors(Executor diskIO, Executor netIO) {
        this.diskIO = diskIO;
        this.netIO = netIO;
    }

    public AppExecutors() {
        this(Executors.newSingleThreadExecutor(), Executors.newFixedThreadPool(3));
    }

    public Executor diskIO() {
        return diskIO;
    }

    public Executor netIO() {
        return netIO;
    }
}
