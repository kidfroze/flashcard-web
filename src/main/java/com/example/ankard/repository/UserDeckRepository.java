package com.example.ankard.repository;

import com.example.ankard.model.UserDeck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserDeckRepository extends JpaRepository<UserDeck, Integer> {

    Optional<UserDeck> findByUser_UserIdAndDeck_DeckId(Integer userId, Integer deckId);

    boolean existsByUser_UserIdAndDeck_DeckId(Integer userId, Integer deckId);
}
