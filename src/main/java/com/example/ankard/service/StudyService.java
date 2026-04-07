package com.example.ankard.service;

import com.example.ankard.dto.StudySessionDTO;
import com.example.ankard.model.*;
import com.example.ankard.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudyService {

        private final FlashcardRepository flashcardRepository;
        private final FlashcardProgressRepository progressRepository;
        private final ReviewHistoryRepository reviewHistoryRepository;
        private final UserRepository userRepository;
        private final DeckRepository deckRepository;
        private final SpacedRepetitionService srsService;

        /**
         * Lấy flashcard tiếp theo cần học trong deck.
         * Trả về null nếu không còn card nào cần học.
         */
        public StudySessionDTO getNextCard(Integer deckId, Integer userId) {
                Deck deck = deckRepository.findById(deckId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy deck"));

                List<Flashcard> dueCards = flashcardRepository.findDueFlashcards(
                                deckId, userId, LocalDateTime.now(), FlashcardProgress.Status.MASTERED);

                if (dueCards.isEmpty()) {
                        return null; // Hoàn thành session
                }

                Flashcard card = dueCards.get(0);
                int remaining = dueCards.size();

                return StudySessionDTO.builder()
                                .deckId(deckId)
                                .deckTitle(deck.getTitle())
                                .flashcardId(card.getFlashcardId())
                                .frontContent(card.getFrontContent())
                                .backContent(card.getBackContent())
                                .exampleSentence(card.getExampleSentence())
                                .pronunciation(card.getPronunciation())
                                .imageUrl(card.getImageUrl())
                                .audioUrl(card.getAudioUrl())
                                .remainingCards(remaining)
                                .totalDue(remaining)
                                .showAnswer(false)
                                .build();
        }

        /**
         * Xử lý kết quả review của user (again/hard/good/easy).
         * Cập nhật FlashcardProgress và lưu ReviewHistory.
         */
        @Transactional
        public void submitReview(Integer flashcardId, Integer userId,
                        ReviewHistory.ReviewResult result) {

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
                Flashcard card = flashcardRepository.findById(flashcardId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy flashcard"));

                // Lấy hoặc tạo mới progress
                FlashcardProgress progress = progressRepository
                                .findByUser_UserIdAndFlashcard_FlashcardId(userId, flashcardId)
                                .orElseGet(() -> createNewProgress(user, card));

                // Áp dụng thuật toán SRS
                srsService.applyReview(progress, result);
                progressRepository.save(progress);

                // Lưu lịch sử
                ReviewHistory history = ReviewHistory.builder()
                                .user(user)
                                .flashcard(card)
                                .reviewResult(result)
                                .reviewedAt(LocalDateTime.now())
                                .build();
                reviewHistoryRepository.save(history);
        }

        private FlashcardProgress createNewProgress(User user, Flashcard card) {
                return FlashcardProgress.builder()
                                .user(user)
                                .flashcard(card)
                                .reviewCount(0)
                                .correctCount(0)
                                .wrongCount(0)
                                .masteryLevel(0)
                                .easeFactor(new BigDecimal("2.50"))
                                .intervalDays(1)
                                .status(FlashcardProgress.Status.NEW)
                                .build();
        }

        /**
         * Lấy nhãn dự kiến cho từng nút (ví dụ: "<10m", "3d", "5d")
         */
        public String[] getNextReviewLabels(Integer flashcardId, Integer userId) {
                FlashcardProgress progress = progressRepository
                                .findByUser_UserIdAndFlashcard_FlashcardId(userId, flashcardId)
                                .orElseGet(() -> {
                                        FlashcardProgress p = new FlashcardProgress();
                                        p.setIntervalDays(1);
                                        p.setEaseFactor(new BigDecimal("2.50"));
                                        return p;
                                });

                return new String[] {
                                srsService.getNextReviewLabel(progress, ReviewHistory.ReviewResult.again),
                                srsService.getNextReviewLabel(progress, ReviewHistory.ReviewResult.hard),
                                srsService.getNextReviewLabel(progress, ReviewHistory.ReviewResult.good),
                                srsService.getNextReviewLabel(progress, ReviewHistory.ReviewResult.easy)
                };
        }

        /**
         * Lấy thẻ cụ thể (dùng khi lật thẻ để hiển thị đáp án)
         */
        public StudySessionDTO getCardDetail(Integer deckId, Integer flashcardId, Integer userId) {
                Deck deck = deckRepository.findById(deckId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy deck"));

                Flashcard card = flashcardRepository.findById(flashcardId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy flashcard"));

                // Tính số card còn lại
                List<Flashcard> dueCards = flashcardRepository.findDueFlashcards(
                                deckId, userId, LocalDateTime.now(), FlashcardProgress.Status.MASTERED);
                int remaining = dueCards.size();

                return StudySessionDTO.builder()
                                .deckId(deckId)
                                .deckTitle(deck.getTitle())
                                .flashcardId(card.getFlashcardId())
                                .frontContent(card.getFrontContent())
                                .backContent(card.getBackContent())
                                .exampleSentence(card.getExampleSentence())
                                .pronunciation(card.getPronunciation())
                                .imageUrl(card.getImageUrl())
                                .audioUrl(card.getAudioUrl())
                                .remainingCards(remaining)
                                .totalDue(remaining)
                                .showAnswer(false)
                                .build();
        }
}
