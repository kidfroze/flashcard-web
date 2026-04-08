package com.example.ankiclone.service;

import com.example.ankiclone.fsrs.FsrsEngine;
import com.example.ankiclone.fsrs.FsrsReviewGrade;
import com.example.ankiclone.fsrs.FsrsScheduler;
import com.example.ankiclone.fsrs.FsrsWeights;
import com.example.ankiclone.model.FlashcardProgress;
import com.example.ankiclone.model.ReviewHistory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Lịch ôn theo FSRS-4.5 (Stability, Difficulty, Retrievability).
 * Trọng số mặc định theo wiki open-spaced-repetition.
 *
 * <p>Nếu {@code spring.jpa.hibernate.ddl-auto=none}, chạy {@code src/main/resources/db/fsrs-add-columns.sql}.</p>
 */
@Service
public class SpacedRepetitionService {

    private final List<Double> weights = FsrsWeights.DEFAULT_W;
    private final double targetRetention = FsrsEngine.DEFAULT_TARGET_RETENTION;

    public void applyReview(FlashcardProgress progress, ReviewHistory.ReviewResult result) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime previousLastReview = progress.getLastReview();
        Double prevS = progress.getFsrsStability();
        Double prevD = progress.getFsrsDifficulty();

        progress.setReviewCount(progress.getReviewCount() + 1);
        if (result == ReviewHistory.ReviewResult.again) {
            progress.setWrongCount(progress.getWrongCount() + 1);
        } else {
            progress.setCorrectCount(progress.getCorrectCount() + 1);
        }

        FsrsReviewGrade grade = mapGrade(result);
        FsrsScheduler.ScheduleOutcome out = FsrsScheduler.scheduleAfterReview(
                now,
                previousLastReview,
                prevS,
                prevD,
                grade,
                weights,
                targetRetention
        );

        progress.setLastReview(now);
        progress.setFsrsStability(out.newStabilityDays());
        progress.setFsrsDifficulty(out.newDifficulty());
        LocalDateTime next = result == ReviewHistory.ReviewResult.again
                ? now.plusMinutes(10)
                : out.nextReview();
        double intervalForStatus = result == ReviewHistory.ReviewResult.again
                ? 10.0 / (24.0 * 60.0)
                : out.scheduledIntervalDays();
        progress.setNextReview(next);
        progress.setIntervalDays(intervalDaysStored(intervalForStatus));
        progress.setStatus(resolveStatus(intervalForStatus));
        if (intervalForStatus >= 21.0) {
            progress.setMasteryLevel(progress.getMasteryLevel() + 1);
        }
    }

    public String getNextReviewLabel(FlashcardProgress progress, ReviewHistory.ReviewResult result) {
        LocalDateTime now = LocalDateTime.now();
        FsrsReviewGrade grade = mapGrade(result);
        FsrsScheduler.ScheduleOutcome out = FsrsScheduler.scheduleAfterReview(
                now,
                progress.getLastReview(),
                progress.getFsrsStability(),
                progress.getFsrsDifficulty(),
                grade,
                weights,
                targetRetention
        );
        if (result == ReviewHistory.ReviewResult.again) {
            return "<10m";
        }
        return formatIntervalLabel(out.scheduledIntervalDays(), out.nextReview(), now);
    }

    private static FsrsReviewGrade mapGrade(ReviewHistory.ReviewResult result) {
        return switch (result) {
            case again -> FsrsReviewGrade.AGAIN;
            case hard -> FsrsReviewGrade.HARD;
            case good -> FsrsReviewGrade.GOOD;
            case easy -> FsrsReviewGrade.EASY;
        };
    }

    private static FlashcardProgress.Status resolveStatus(double scheduledDays) {
        if (scheduledDays < 1.0) {
            return FlashcardProgress.Status.LEARNING;
        }
        if (scheduledDays >= 21.0) {
            return FlashcardProgress.Status.MASTERED;
        }
        return FlashcardProgress.Status.REVIEW;
    }

    private static int intervalDaysStored(double scheduledDays) {
        if (scheduledDays < 1.0) {
            return 0;
        }
        return Math.max(1, (int) Math.round(scheduledDays));
    }

    private static String formatIntervalLabel(double scheduledDays, LocalDateTime nextReview, LocalDateTime now) {
        if (scheduledDays < 1.0 / 96.0) {
            long minutes = Math.max(1, java.time.Duration.between(now, nextReview).toMinutes());
            return "<" + Math.max(10, minutes) + "m";
        }
        if (scheduledDays < 1.0) {
            long minutes = Math.max(1, java.time.Duration.between(now, nextReview).toMinutes());
            return minutes + "m";
        }
        return Math.max(1, Math.round(scheduledDays)) + "d";
    }
}
