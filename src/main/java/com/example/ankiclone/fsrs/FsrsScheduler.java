package com.example.ankiclone.fsrs;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Lên lịch ôn tiếp theo từ trạng thái FSRS + thời điểm ôn trước.
 * Tách khỏi JPA để dễ unit test.
 */
public final class FsrsScheduler {

    private FsrsScheduler() {}

    public record ScheduleOutcome(
            LocalDateTime nextReview,
            double newStabilityDays,
            double newDifficulty,
            double scheduledIntervalDays
    ) {}

    /**
     * @param lastReview     null nếu chưa từng ôn
     * @param stabilityDays  null nếu chưa khởi tạo FSRS
     * @param difficulty     null nếu chưa khởi tạo FSRS
     */
    public static ScheduleOutcome scheduleAfterReview(
            LocalDateTime now,
            LocalDateTime lastReview,
            Double stabilityDays,
            Double difficulty,
            FsrsReviewGrade grade,
            List<Double> weights,
            double targetRetention
    ) {
        double elapsedDays = elapsedDays(lastReview, now);
        int g = grade.intValue();

        final double newS;
        final double newD;

        if (stabilityDays == null || difficulty == null) {
            newS = FsrsEngine.initialStability(g, weights);
            newD = FsrsEngine.initialDifficulty(g, weights);
        } else {
            double r = FsrsEngine.retrievability(elapsedDays, stabilityDays, weights);
            newD = FsrsEngine.nextDifficulty(difficulty, g, weights);
            if (grade == FsrsReviewGrade.AGAIN) {
                newS = FsrsEngine.stabilityAfterLapse(stabilityDays, newD, r, weights);
            } else {
                newS = FsrsEngine.stabilityAfterSuccess(stabilityDays, newD, r, g, weights);
            }
        }

        double goodI = FsrsEngine.nextIntervalDays(newS, targetRetention, weights);
        double scheduledDays = FsrsEngine.intervalDaysForGrade(goodI, grade);
        LocalDateTime next = addInterval(now, scheduledDays);

        return new ScheduleOutcome(next, newS, newD, scheduledDays);
    }

    public static double elapsedDays(LocalDateTime lastReview, LocalDateTime now) {
        if (lastReview == null) {
            return 0.0;
        }
        long seconds = Duration.between(lastReview, now).getSeconds();
        return Math.max(0.0, seconds / 86400.0);
    }

    /**
     * Chuyển khoảng cách (ngày, có phần thập phân) sang {@link LocalDateTime}.
     * Giá trị rất nhỏ được làm tròn tối thiểu vài phút để UX học tập hợp lý.
     */
    public static LocalDateTime addInterval(LocalDateTime from, double intervalDays) {
        if (intervalDays < 1.0 / 96.0) {
            long minutes = Math.max(10, Math.round(intervalDays * 24.0 * 60.0));
            return from.plusMinutes(minutes);
        }
        if (intervalDays < 1.0) {
            long minutes = Math.round(intervalDays * 24.0 * 60.0);
            return from.plusMinutes(Math.max(30, minutes));
        }
        long daysRounded = Math.max(1, Math.round(intervalDays));
        return from.plusDays(daysRounded);
    }
}
