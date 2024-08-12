package com.example.demo.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class TimeUtil {

    public static long nowMillis() {
        return toMillis(LocalDateTime.now(ZoneOffset.UTC));
    }

    public static long toMillis(LocalDateTime value) {
        return value.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public static LocalDateTime toLocalDateTime(long value) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneOffset.UTC);
    }
}
