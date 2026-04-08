package com.example.ankiclone.fsrs;

import java.util.Arrays;
import java.util.List;

/**
 * Trọng số mặc định FSRS-4.5 (17 tham số), đồng bộ wiki:
 * <a href="https://github.com/open-spaced-repetition/awesome-fsrs/wiki/The-Algorithm">The Algorithm</a>
 */
public final class FsrsWeights {

    private FsrsWeights() {}

    /** w0..w16 — FSRS-4.5 default */
    public static final List<Double> DEFAULT_W = Arrays.asList(
            0.4872, 1.4003, 3.7145, 13.8206,
            5.1618, 1.2298, 0.8975, 0.031,
            1.6474, 0.1367, 1.0461, 2.1072,
            0.0793, 0.3246, 1.587, 0.2272, 2.8755
    );

    /** Đường cong quên FSRS-4.5: DECAY = -0.5, FACTOR = 19/81 */
    public static final double DECAY = -0.5;
    public static final double FACTOR = 19.0 / 81.0;
}
