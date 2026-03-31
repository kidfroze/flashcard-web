package com.example.ankiclone.repository;

import com.example.ankiclone.model.Flashcard;
import com.example.ankiclone.model.FlashcardProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, Integer> {

    // Lấy tất cả flashcard trong một deck
    List<Flashcard> findByDeck_DeckId(Integer deckId);

    // Đếm số flashcard trong deck
    long countByDeck_DeckId(Integer deckId);

    // Lấy flashcard cần ôn (next_review <= now hoặc chưa có progress)
    @Query("""
        SELECT f FROM Flashcard f
        WHERE f.deck.deckId = :deckId
          AND (
            f.flashcardId NOT IN (
                SELECT fp.flashcard.flashcardId FROM FlashcardProgress fp
                WHERE fp.user.userId = :userId
            )
            OR f.flashcardId IN (
                SELECT fp.flashcard.flashcardId FROM FlashcardProgress fp
                WHERE fp.user.userId = :userId
                  AND (fp.nextReview IS NULL OR fp.nextReview <= :now)
                  AND fp.status <> :masteredStatus
            )
          )
        ORDER BY f.flashcardId
    """)
    List<Flashcard> findDueFlashcards(
        @Param("deckId") Integer deckId,
        @Param("userId") Integer userId,
        @Param("now") LocalDateTime now,
        @Param("masteredStatus") FlashcardProgress.Status masteredStatus
    );
}
