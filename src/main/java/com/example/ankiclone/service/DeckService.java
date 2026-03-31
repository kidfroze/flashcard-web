package com.example.ankiclone.service;

import com.example.ankiclone.dto.DeckFormDTO;
import com.example.ankiclone.dto.DeckSummaryDTO;
import com.example.ankiclone.model.Deck;
import com.example.ankiclone.model.Flashcard;
import com.example.ankiclone.model.User;
import com.example.ankiclone.model.UserDeck;
import com.example.ankiclone.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

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
        long total = flashcardRepository.countByDeck_DeckId(deck.getDeckId());
        long newCards = flashcardProgressRepository.countNewCards(deck.getDeckId(), userId);
        long learning = flashcardProgressRepository.countLearningCards(deck.getDeckId(), userId);
        long review = flashcardProgressRepository.countReviewCards(deck.getDeckId(), userId);

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

    public Deck getPublicDeckById(Integer deckId) {
        Deck deck = getDeckById(deckId);
        if (deck.getIsPublic() == null || !deck.getIsPublic()) {
            throw new RuntimeException("Deck này không được chia sẻ công khai.");
        }
        return deck;
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

    /**
     * Import (copy) một deck public về tài khoản user hiện tại.
     * - Tạo deck mới (created_by = current user)
     * - Copy toàn bộ flashcards sang deck mới
     * - Deck mới mặc định là private để tránh chia sẻ ngoài ý muốn
     */
    @Transactional
    public Deck importPublicDeck(Integer sourceDeckId, Integer userId) {
        Deck sourceDeck = getPublicDeckById(sourceDeckId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user id=" + userId));

        Deck newDeck = Deck.builder()
                .createdBy(user)
                .title(sourceDeck.getTitle())
                .description(sourceDeck.getDescription())
                .isPublic(false)
                .build();

        newDeck = deckRepository.save(newDeck);

        List<Flashcard> sourceCards = flashcardRepository.findByDeck_DeckId(sourceDeckId);
        List<Flashcard> copies = new ArrayList<>(sourceCards.size());
        for (Flashcard c : sourceCards) {
            copies.add(Flashcard.builder()
                    .deck(newDeck)
                    .frontContent(c.getFrontContent())
                    .backContent(c.getBackContent())
                    .exampleSentence(c.getExampleSentence())
                    .pronunciation(c.getPronunciation())
                    .imageUrl(c.getImageUrl())
                    .audioUrl(c.getAudioUrl())
                    .build());
        }
        flashcardRepository.saveAll(copies);

        return newDeck;
    }

    // ========== DECK CÔNG KHAI (KHÁM PHÁ) ==========

    public List<DeckSummaryDTO> getPublicDecks(Integer userId) {
        List<Deck> publicDecks = deckRepository.findByIsPublicTrue();
        return publicDecks.stream()
                .map(deck -> buildDeckSummary(deck, userId))
                .collect(Collectors.toList());
    }

    @Transactional
    public void hidePublicDeck(Integer deckId) {
        Deck deck = getDeckById(deckId);
        deck.setIsPublic(false);
        deckRepository.save(deck);
    }
}
