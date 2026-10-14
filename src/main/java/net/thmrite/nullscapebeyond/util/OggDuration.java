package net.thmrite.nullscapebeyond.util;

import java.io.IOException;
import java.io.InputStream;

public final class OggDuration {
    public static int ticks(String resourcePath, int fallbackTicks) {
        try (InputStream in = OggDuration.class.getResourceAsStream(resourcePath)) {
            if (in == null) return fallbackTicks;
            byte[] d = in.readAllBytes();

            int sampleRate = -1;
            for (int i = 0; i < d.length - 16; i++) {
                if (d[i] == 1 && d[i + 1] == 'v' && d[i + 2] == 'o' && d[i + 3] == 'r'
                        && d[i + 4] == 'b' && d[i + 5] == 'i' && d[i + 6] == 's') {
                    sampleRate = (int) readLE(d, i + 12, 4);
                    break;
                }
            }
            long granule = -1;
            for (int i = d.length - 14; i >= 0; i--) {
                if (d[i] == 'O' && d[i + 1] == 'g' && d[i + 2] == 'g' && d[i + 3] == 'S') {
                    granule = readLE(d, i + 6, 8);
                    break;
                }
            }
            if (sampleRate <= 0 || granule <= 0) return fallbackTicks;
            return (int) Math.ceil(granule * 20.0D / sampleRate) + 2;
        } catch (IOException e) {
            return fallbackTicks;
        }
    }

    private static long readLE(byte[] d, int offset, int bytes) {
        long v = 0;
        for (int i = bytes - 1; i >= 0; i--) v = (v << 8) | (d[offset + i] & 0xFF);
        return v;
    }

    private OggDuration() {}
}