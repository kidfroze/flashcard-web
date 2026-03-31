package com.example.ankiclone.service;

import com.example.ankiclone.model.FlashcardProgress;
import com.example.ankiclone.model.ReviewHistory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Thuật toán Spaced Repetition đơn giản dựa trên SM-2.
 *
 * Quy tắc:
 *  - again  → quay lại học sau <10 phút, status = LEARNING
 *  - hard   → ôn sau <15 phút (interval giữ nguyên, easeFactor giảm)
 *  - good   → ôn theo interval hiện tại * easeFactor
 *  - easy   → ôn sau interval * easeFactor * 1.3, easeFactor tăng
 */
@Service
public class SpacedRepetitionService {

    private static final BigDecimal MIN_EASE = new BigDecimal("1.30");
    private static final BigDecimal EASE_BONUS = new BigDecimal("0.15");
    private static final BigDecimal EASE_PENALTY = new BigDecimal("0.20");
    private static final int AGAIN_MINUTES = 10;
    private static final int HARD_MINUTES  = 15;

    public void applyReview(FlashcardProgress progress, ReviewHistory.ReviewResult result) {
        LocalDateTime now = LocalDateTime.now();
        progress.setLastReview(now);
        progress.setReviewCount(progress.getReviewCount() + 1);

        switch (result) {
            case again -> applyAgain(progress, now);
            case hard  -> applyHard(progress, now);
            case good  -> applyGood(progress, now);
            case easy  -> applyEasy(progress, now);
        }
    }

    // ========== LOGIC TỪNG NÚT ==========

    private void applyAgain(FlashcardProgress p, LocalDateTime now) {
        p.setWrongCount(p.getWrongCount() + 1);
        p.setIntervalDays(1);
        p.setStatus(FlashcardProgress.Status.LEARNING);
        p.setNextReview(now.plusMinutes(AGAIN_MINUTES));

        // Giảm ease factor
        BigDecimal newEase = p.getEaseFactor().subtract(EASE_PENALTY);
        p.setEaseFactor(newEase.max(MIN_EASE).setScale(2, RoundingMode.HALF_UP));
    }

    private void applyHard(FlashcardProgress p, LocalDateTime now) {
        p.setWrongCount(p.getWrongCount() + 1);
        p.setStatus(FlashcardProgress.Status.LEARNING);
        p.setNextReview(now.plusMinutes(HARD_MINUTES));

        // Giảm ease factor nhẹ hơn again
        BigDecimal newEase = p.getEaseFactor().subtract(new BigDecimal("0.15"));
        p.setEaseFactor(newEase.max(MIN_EASE).setScale(2, RoundingMode.HALF_UP));
        // interval giữ nguyên
    }

    private void applyGood(FlashcardProgress p, LocalDateTime now) {
        p.setCorrectCount(p.getCorrectCount() + 1);

        int newInterval;
        if (p.getIntervalDays() <= 1) {
            newInterval = 1; // lần đầu good → 1 ngày
        } else {
            // interval mới = interval cũ * easeFactor
            newInterval = (int) Math.round(
                p.getIntervalDays() * p.getEaseFactor().doubleValue()
            );
        }

        p.setIntervalDays(newInterval);
        p.setNextReview(now.plusDays(newInterval));

        if (newInterval >= 21) {
            p.setStatus(FlashcardProgress.Status.MASTERED);
            p.setMasteryLevel(p.getMasteryLevel() + 1);
        } else {
            p.setStatus(FlashcardProgress.Status.REVIEW);
        }
    }

    private void applyEasy(FlashcardProgress p, LocalDateTime now) {
        p.setCorrectCount(p.getCorrectCount() + 1);

        // Tăng ease factor
        BigDecimal newEase = p.getEaseFactor().add(EASE_BONUS).setScale(2, RoundingMode.HALF_UP);
        p.setEaseFactor(newEase);

        int newInterval = (int) Math.round(
            p.getIntervalDays() * newEase.doubleValue() * 1.3
        );
        if (newInterval < 4) newInterval = 4; // tối thiểu 4 ngày với easy

        p.setIntervalDays(newInterval);
        p.setNextReview(now.plusDays(newInterval));
        p.setStatus(FlashcardProgress.Status.REVIEW);
        p.setMasteryLevel(p.getMasteryLevel() + 1);
    }

    // ========== HELPER: tính interval dự kiến để hiển thị trên nút ==========

    public String getNextReviewLabel(FlashcardProgress progress, ReviewHistory.ReviewResult result) {
        return switch (result) {
            case again -> "<" + AGAIN_MINUTES + "m";
            case hard  -> "<" + HARD_MINUTES + "m";
            case good  -> {
                int interval = progress.getIntervalDays() <= 1 ? 1
                    : (int) Math.round(progress.getIntervalDays() * progress.getEaseFactor().doubleValue());
                yield interval + "d";
            }
            case easy  -> {
                double easeAfter = progress.getEaseFactor().add(EASE_BONUS).doubleValue();
                int interval = (int) Math.round(progress.getIntervalDays() * easeAfter * 1.3);
                if (interval < 4) interval = 4;
                yield interval + "d";
            }
        };
    }
}
