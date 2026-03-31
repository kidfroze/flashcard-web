package com.example.ankiclone.repository;

import com.example.ankiclone.model.FlashcardProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FlashcardProgressRepository extends JpaRepository<FlashcardProgress, Integer> {

    Optional<FlashcardProgress> findByUser_UserIdAndFlashcard_FlashcardId(
        Integer userId, Integer flashcardId
    );

    // Đếm số card "new" (chưa có progress) trong deck
    @Query("""
        SELECT COUNT(f) FROM Flashcard f
        WHERE f.deck.deckId = :deckId
          AND f.flashcardId NOT IN (
              SELECT fp.flashcard.flashcardId FROM FlashcardProgress fp
              WHERE fp.user.userId = :userId
          )
    """)
    long countNewCards(@Param("deckId") Integer deckId, @Param("userId") Integer userId);

    // Đếm số card "learning" trong deck
    @Query("""
        SELECT COUNT(fp) FROM FlashcardProgress fp
        WHERE fp.flashcard.deck.deckId = :deckId
          AND fp.user.userId = :userId
          AND fp.status = 'LEARNING'
    """)
    long countLearningCards(@Param("deckId") Integer deckId, @Param("userId") Integer userId);

    // Đếm số card "review" trong deck
    @Query("""
        SELECT COUNT(fp) FROM FlashcardProgress fp
        WHERE fp.flashcard.deck.deckId = :deckId
          AND fp.user.userId = :userId
          AND fp.status = 'REVIEW'
    """)
    long countReviewCards(@Param("deckId") Integer deckId, @Param("userId") Integer userId);
}
