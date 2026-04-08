package com.example.ankiclone.fsrs;

import java.util.List;

/**
 * Thuần toán FSRS-4.5: Retrievability, Stability, Difficulty, khoảng cách ôn tiếp theo.
 * Công thức theo wiki: <a href="https://github.com/open-spaced-repetition/awesome-fsrs/wiki/The-Algorithm">FSRS-4.5</a>
 */
public final class FsrsEngine {

    /** Mức nhớ mục tiêu khi lên lịch (Anki mặc định ~0.9). */
    public static final double DEFAULT_TARGET_RETENTION = 0.9;

    /** Hệ số khoảng cách so với Good (tương tự Anki). */
    public static final double HARD_INTERVAL_RATIO = 0.8;
    public static final double EASY_INTERVAL_RATIO = 1.3;

    /** Ngày tối thiểu khi chuyển sang lịch theo ngày (tránh 0 ngày). */
    public static final double MIN_SCHEDULE_DAYS = 1.0 / 1440.0 * 10; // ~10 phút tính theo ngày

    private FsrsEngine() {}

    /**
     * Retrievability sau t ngày kể từ lần ôn trước:
     * R(t,S) = (1 + FACTOR * t/S)^DECAY
     */
    public static double retrievability(double elapsedDays, double stabilityDays, List<Double> w) {
        validateWeights(w);
        if (elapsedDays <= 0) {
            return 1.0;
        }
        if (stabilityDays <= 0) {
            throw new IllegalArgumentException("stabilityDays must be positive for retrievability");
        }
        double base = 1.0 + FsrsWeights.FACTOR * (elapsedDays / stabilityDays);
        return Math.pow(base, FsrsWeights.DECAY);
    }

    /**
     * Khoảng cách ôn (ngày) để đạt mức nhớ mục tiêu r:
     * I(r,S) = S/FACTOR * (r^(1/DECAY) - 1)
     */
    public static double nextIntervalDays(double stabilityDays, double targetRetention, List<Double> w) {
        validateWeights(w);
        if (stabilityDays <= 0) {
            throw new IllegalArgumentException("stabilityDays must be positive");
        }
        if (targetRetention <= 0 || targetRetention >= 1) {
            throw new IllegalArgumentException("targetRetention must be in (0, 1)");
        }
        double invDecay = 1.0 / FsrsWeights.DECAY;
        return (stabilityDays / FsrsWeights.FACTOR) * (Math.pow(targetRetention, invDecay) - 1.0);
    }

    /** Độ khó ban đầu sau lần đánh giá đầu tiên: D0(G) = w4 - (G-3)*w5 */
    public static double initialDifficulty(int grade, List<Double> w) {
        validateWeights(w);
        double g = grade;
        return clampDifficulty(w.get(4) - (g - 3.0) * w.get(5));
    }

    /** Độ ổn định ban đầu: S0(G) = w_{G-1} */
    public static double initialStability(int grade, List<Double> w) {
        validateWeights(w);
        if (grade < 1 || grade > 4) {
            throw new IllegalArgumentException("grade must be 1..4");
        }
        return Math.max(MIN_SCHEDULE_DAYS, w.get(grade - 1));
    }

    /**
     * Cập nhật độ khó sau ôn: D' = w7*D0(3) + (1-w7)*(D - w6*(G-3)), D0(3)=w4
     */
    public static double nextDifficulty(double currentD, int grade, List<Double> w) {
        validateWeights(w);
        double g = grade;
        double d0AtGood = w.get(4);
        double dPrime = w.get(7) * d0AtGood + (1.0 - w.get(7)) * (currentD - w.get(6) * (g - 3.0));
        return clampDifficulty(dPrime);
    }

    /**
     * Stability sau ôn thành công (Hard/Good/Easy), FSRS v4:
     * S' = S * (e^w8 * (11-D) * S^(-w9) * (e^(w10*(1-R))-1) * w15(if G=2) * w16(if G=4) + 1)
     */
    public static double stabilityAfterSuccess(double s, double d, double r, int grade, List<Double> w) {
        validateWeights(w);
        if (s <= 0 || d < 1 || d > 10 || r < 0 || r > 1) {
            throw new IllegalArgumentException("invalid s, d, or r");
        }
        double hardMul = (grade == 2) ? w.get(15) : 1.0;
        double easyMul = (grade == 4) ? w.get(16) : 1.0;
        double inner = Math.exp(w.get(8))
                * (11.0 - d)
                * Math.pow(s, -w.get(9))
                * (Math.exp(w.get(10) * (1.0 - r)) - 1.0)
                * hardMul
                * easyMul
                + 1.0;
        return Math.max(MIN_SCHEDULE_DAYS, s * inner);
    }

    /**
     * Stability sau quên (Again): S' = w11 * D^(-w12) * ((S+1)^w13 - 1) * e^(w14*(1-R))
     */
    public static double stabilityAfterLapse(double s, double d, double r, List<Double> w) {
        validateWeights(w);
        if (s <= 0 || d < 1 || d > 10 || r < 0 || r > 1) {
            throw new IllegalArgumentException("invalid s, d, or r");
        }
        double lapse = w.get(11)
                * Math.pow(d, -w.get(12))
                * (Math.pow(s + 1.0, w.get(13)) - 1.0)
                * Math.exp(w.get(14) * (1.0 - r));
        return Math.max(MIN_SCHEDULE_DAYS, lapse);
    }

    /** Khoảng cách hiển thị theo grade (Hard ngắn hơn Good, Easy dài hơn). */
    public static double intervalDaysForGrade(double goodIntervalDays, FsrsReviewGrade grade) {
        return switch (grade) {
            case AGAIN -> goodIntervalDays; // Again xử lý ở scheduler (lapse S đã nhỏ)
            case HARD -> Math.max(MIN_SCHEDULE_DAYS, goodIntervalDays * HARD_INTERVAL_RATIO);
            case GOOD -> goodIntervalDays;
            case EASY -> goodIntervalDays * EASY_INTERVAL_RATIO;
        };
    }

    public static double clampDifficulty(double d) {
        return Math.min(10.0, Math.max(1.0, d));
    }

    private static void validateWeights(List<Double> w) {
        if (w == null || w.size() < 17) {
            throw new IllegalArgumentException("weights must contain at least 17 values (FSRS-4.5)");
        }
    }
}
