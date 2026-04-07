package com.example.ankard.repository;

import com.example.ankard.model.FlashcardProgress;
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
    long countByFlashcard_Deck_DeckIdAndUser_UserIdAndStatus(
        Integer deckId,
        Integer userId,
        FlashcardProgress.Status status
    );

    // Backwards-compatible helper method names (optional)
    default long countLearningCards(Integer deckId, Integer userId) {
        return countByFlashcard_Deck_DeckIdAndUser_UserIdAndStatus(deckId, userId, FlashcardProgress.Status.LEARNING);
    }

    default long countReviewCards(Integer deckId, Integer userId) {
        return countByFlashcard_Deck_DeckIdAndUser_UserIdAndStatus(deckId, userId, FlashcardProgress.Status.REVIEW);
    }
}
