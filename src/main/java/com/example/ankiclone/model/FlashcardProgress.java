package com.example.ankiclone.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "flashcard_progress")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FlashcardProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id")
    private Integer progressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flashcard_id", nullable = false)
    private Flashcard flashcard;

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Column(name = "correct_count")
    private Integer correctCount = 0;

    @Column(name = "wrong_count")
    private Integer wrongCount = 0;

    @Column(name = "mastery_level")
    private Integer masteryLevel = 0;

    @Column(name = "ease_factor", precision = 3, scale = 2)
    private BigDecimal easeFactor = new BigDecimal("2.50");

    @Column(name = "interval_days")
    private Integer intervalDays = 1;

    @Column(name = "last_review")
    private LocalDateTime lastReview;

    @Column(name = "next_review")
    private LocalDateTime nextReview;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status = Status.NEW;

    public enum Status {
        NEW("new"), LEARNING("learning"), REVIEW("review"), MASTERED("mastered");

        private final String value;
        Status(String value) { this.value = value; }
        public String getValue() { return value; }
    }
}
