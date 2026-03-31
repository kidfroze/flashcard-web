package com.example.ankiclone.service;

import com.example.ankiclone.dto.DeckFormDTO;
import com.example.ankiclone.dto.DeckSummaryDTO;
import com.example.ankiclone.model.Deck;
import com.example.ankiclone.model.User;
import com.example.ankiclone.model.UserDeck;
import com.example.ankiclone.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeckService {

    private final DeckRepository deckRepository;
    private final UserRepository userRepository;
    private final UserDeckRepository userDeckRepository;
    private final FlashcardRepository flashcardRepository;
    private final FlashcardProgressRepository flashcardProgressRepository;

    // ========== TRANG CHỦ ==========

    /**
     * Lấy tất cả deck của user (do tạo + đã lưu), kèm thống kê số card
     */
    public List<DeckSummaryDTO> getDeckSummariesForUser(Integer userId) {
        List<Deck> decks = deckRepository.findAllDecksForUser(userId);
        return decks.stream()
                .map(deck -> buildDeckSummary(deck, userId))
                .collect(Collectors.toList());
    }

    private DeckSummaryDTO buildDeckSummary(Deck deck, Integer userId) {
        long total    = flashcardRepository.countByDeck_DeckId(deck.getDeckId());
        long newCards = flashcardProgressRepository.countNewCards(deck.getDeckId(), userId);
        long learning = flashcardProgressRepository.countLearningCards(deck.getDeckId(), userId);
        long review   = flashcardProgressRepository.countReviewCards(deck.getDeckId(), userId);

        return DeckSummaryDTO.builder()
                .deckId(deck.getDeckId())
                .title(deck.getTitle())
                .description(deck.getDescription())
                .isPublic(deck.getIsPublic())
                .totalCards(total)
                .newCards(newCards)
                .learningCards(learning)
                .reviewCards(review)
                .build();
    }

    // ========== CRUD DECK ==========

    public Deck getDeckById(Integer deckId) {
        return deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy deck id=" + deckId));
    }

    @Transactional
    public Deck createDeck(DeckFormDTO form, Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user id=" + userId));

        Deck deck = Deck.builder()
                .title(form.getTitle())
                .description(form.getDescription())
                .isPublic(form.getIsPublic() != null ? form.getIsPublic() : true)
                .createdBy(user)
                .build();

        return deckRepository.save(deck);
    }

    @Transactional
    public Deck updateDeck(Integer deckId, DeckFormDTO form) {
        Deck deck = getDeckById(deckId);
        deck.setTitle(form.getTitle());
        deck.setDescription(form.getDescription());
        deck.setIsPublic(form.getIsPublic() != null ? form.getIsPublic() : deck.getIsPublic());
        return deckRepository.save(deck);
    }

    @Transactional
    public void deleteDeck(Integer deckId) {
        deckRepository.deleteById(deckId);
    }

    // ========== LƯU DECK CÔNG KHAI ==========

    @Transactional
    public void saveDeckForUser(Integer deckId, Integer userId) {
        if (userDeckRepository.existsByUser_UserIdAndDeck_DeckId(userId, deckId)) {
            return; // đã lưu rồi
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
        Deck deck = getDeckById(deckId);

        UserDeck userDeck = UserDeck.builder()
                .user(user)
                .deck(deck)
                .isFavorite(false)
                .build();
        userDeckRepository.save(userDeck);
    }

    // ========== DECK CÔNG KHAI (KHÁM PHÁ) ==========

    public List<DeckSummaryDTO> getPublicDecks(Integer userId) {
        List<Deck> publicDecks = deckRepository.findByIsPublicTrue();
        return publicDecks.stream()
                .map(deck -> buildDeckSummary(deck, userId))
                .collect(Collectors.toList());
    }
}
