package com.example.ankard.repository;

import com.example.ankard.model.ReviewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewHistoryRepository extends JpaRepository<ReviewHistory, Integer> {

    List<ReviewHistory> findByUser_UserIdAndFlashcard_FlashcardIdOrderByReviewedAtDesc(
        Integer userId, Integer flashcardId
    );

    List<ReviewHistory> findByUser_UserIdOrderByReviewedAtDesc(Integer userId);
}
