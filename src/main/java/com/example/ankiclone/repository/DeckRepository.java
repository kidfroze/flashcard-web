package com.example.ankiclone.repository;

import com.example.ankiclone.model.Deck;
import com.example.ankiclone.model.DeckStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DeckRepository extends JpaRepository<Deck, Integer> {

    // Lấy deck do user tạo
    List<Deck> findByCreatedBy_UserId(Integer userId);

    List<Deck> findByStatus(DeckStatus status);

    // Tìm kiếm các deck public theo keyword ở title hoặc description
    @Query("SELECT d FROM Deck d WHERE d.status = :status AND " +
            "(LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Deck> searchDecksByStatusAndKeyword(@Param("status") DeckStatus status, @Param("keyword") String keyword);

    // Deck mà user đã lưu (qua UserDecks)
    @Query("SELECT ud.deck FROM UserDeck ud WHERE ud.user.userId = :userId")
    List<Deck> findDecksByUserId(@Param("userId") Integer userId);

    // Deck user tạo HOẶC đã lưu (trang chủ hiển thị)
    @Query("""
        SELECT DISTINCT d FROM Deck d
        WHERE d.createdBy.userId = :userId
           OR d.deckId IN (SELECT ud.deck.deckId FROM UserDeck ud WHERE ud.user.userId = :userId)
        ORDER BY d.updatedAt DESC
    """)
    List<Deck> findAllDecksForUser(@Param("userId") Integer userId);
}
