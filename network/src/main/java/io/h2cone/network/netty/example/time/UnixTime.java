package io.h2cone.network.netty.example.time;

import java.util.Date;

/**
 * https://netty.io/wiki/user-guide-for-4.x.html
 */
public class UnixTime {
    private final long value;

    public UnixTime(long value) {
        this.value = value;
    }

    public UnixTime() {
        this(System.currentTimeMillis() / 1000L + 2208988800L);
    }

    public long value() {
        return value;
    }

    @Override
    public String toString() {
        return new Date((value() - 2208988800L) * 1000L).toString();
    }
}
