package com.example.ankiclone.fsrs;

/**
 * Thang đánh giá FSRS: 1 Again, 2 Hard, 3 Good, 4 Easy.
 */
public enum FsrsReviewGrade {
    AGAIN(1),
    HARD(2),
    GOOD(3),
    EASY(4);

    private final int value;

    FsrsReviewGrade(int value) {
        this.value = value;
    }

    public int intValue() {
        return value;
    }
}
