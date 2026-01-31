package com.cvyuh.utils.misc;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public final class Timed {

    private Timed() {}

    public static <T> Result<T> run(Supplier<T> supplier) {
        long start = System.nanoTime();
        T value = supplier.get();
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        return new Result<>(value, elapsedMs);
    }

    public record Result<T>(T value, long elapsedMs) {}
}
