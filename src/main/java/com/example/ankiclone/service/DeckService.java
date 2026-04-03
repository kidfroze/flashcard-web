package com.example.ankiclone.service;

import com.example.ankiclone.dto.DeckFormDTO;
import com.example.ankiclone.dto.DeckSummaryDTO;
import com.example.ankiclone.model.Deck;
import com.example.ankiclone.model.DeckStatus;
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
                .status(deck.getStatus())
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
        if (deck.getStatus() != DeckStatus.APPROVED) {
            throw new RuntimeException("Deck này không được chia sẻ công khai.");
        }
        return deck;
    }

    public void ensureDeckEditable(Integer deckId) {
        Deck deck = getDeckById(deckId);
        assertDeckEditable(deck);
    }

    @Transactional
    public Deck createDeck(DeckFormDTO form, Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user id=" + userId));

        Deck deck = Deck.builder()
                .title(form.getTitle())
                .description(form.getDescription())
                .status(DeckStatus.PRIVATE)
                .createdBy(user)
                .build();

        return deckRepository.save(deck);
    }

    @Transactional
    public Deck updateDeck(Integer deckId, DeckFormDTO form) {
        Deck deck = getDeckById(deckId);
        assertDeckEditable(deck);
        deck.setTitle(form.getTitle());
        deck.setDescription(form.getDescription());
        return deckRepository.save(deck);
    }

    @Transactional
    public void deleteDeck(Integer deckId) {
        Deck deck = getDeckById(deckId);
        assertDeckEditable(deck);
        deckRepository.delete(deck);
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
                .status(DeckStatus.PRIVATE)
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
        List<Deck> publicDecks = deckRepository.findByStatus(DeckStatus.APPROVED);
        return publicDecks.stream()
                .map(deck -> buildDeckSummary(deck, userId))
                .collect(Collectors.toList());
    }

    public List<DeckSummaryDTO> searchPublicDecks(Integer userId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getPublicDecks(userId);
        }
        String query = keyword.trim();

        // Bước 1: vào DB tìm theo tiêu đề hoặc mô tả, chỉ các deck public
        List<Deck> publicDecks = deckRepository.searchDecksByStatusAndKeyword(DeckStatus.APPROVED, query);

        // Bước 2: nếu không có kết quả hoặc cần xử lý ký tự dấu tiếng Việt, lọc bằng Java không dấu
        if (publicDecks.isEmpty()) {
            String normalizedQuery = normalize(query);
            publicDecks = deckRepository.findByStatus(DeckStatus.APPROVED).stream()
                    .filter(deck -> containsIgnoreCaseAndAccent(deck.getTitle(), query, normalizedQuery)
                            || containsIgnoreCaseAndAccent(deck.getDescription(), query, normalizedQuery))
                    .toList();
        }

        return publicDecks.stream()
                .map(deck -> buildDeckSummary(deck, userId))
                .collect(Collectors.toList());
    }

    private boolean containsIgnoreCaseAndAccent(String value, String query, String normalizedQuery) {
        if (value == null) {
            return false;
        }
        String lowerValue = value.toLowerCase();
        if (lowerValue.contains(query.toLowerCase())) {
            return true;
        }
        String normalizedValue = normalize(value);
        return normalizedValue.contains(normalizedQuery);
    }

    private String normalize(String input) {
        if (input == null) {
            return "";
        }
        String normalized = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "").toLowerCase();
    }

    @Transactional
    public void hidePublicDeck(Integer deckId) {
        Deck deck = getDeckById(deckId);
        deck.setStatus(DeckStatus.REJECTED);
        deckRepository.save(deck);
    }

    @Transactional
    public void submitDeckForReview(Integer deckId, Integer userId) {
        Deck deck = getDeckById(deckId);
        if (!deck.getCreatedBy().getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền gửi duyệt deck này.");
        }
        if (deck.getStatus() == DeckStatus.PENDING) {
            throw new RuntimeException("Deck này đang chờ admin duyệt.");
        }
        if (deck.getStatus() == DeckStatus.APPROVED) {
            throw new RuntimeException("Deck này đã được duyệt công khai.");
        }
        deck.setStatus(DeckStatus.PENDING);
        deckRepository.save(deck);
    }

    public List<Deck> getPendingDecks() {
        return deckRepository.findByStatus(DeckStatus.PENDING);
    }

    @Transactional
    public void approveDeck(Integer deckId) {
        Deck deck = getDeckById(deckId);
        deck.setStatus(DeckStatus.APPROVED);
        deckRepository.save(deck);
    }

    @Transactional
    public void rejectDeck(Integer deckId) {
        Deck deck = getDeckById(deckId);
        deck.setStatus(DeckStatus.REJECTED);
        deckRepository.save(deck);
    }

    private void assertDeckEditable(Deck deck) {
        if (deck.getStatus() == DeckStatus.PENDING) {
            throw new RuntimeException("Deck đang chờ duyệt, bạn không thể chỉnh sửa lúc này.");
        }
    }
}
