package com.example.ankard.service;

import com.example.ankard.dto.FlashcardFormDTO;
import com.example.ankard.model.Deck;
import com.example.ankard.model.DeckStatus;
import com.example.ankard.model.Flashcard;
import com.example.ankard.repository.DeckRepository;
import com.example.ankard.repository.FlashcardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final DeckRepository deckRepository;

    public List<Flashcard> getFlashcardsByDeck(Integer deckId) {
        return flashcardRepository.findByDeck_DeckId(deckId);
    }

    public Flashcard getFlashcardById(Integer flashcardId) {
        return flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy flashcard id=" + flashcardId));
    }

    @Transactional
    public Flashcard createFlashcard(Integer deckId, FlashcardFormDTO form) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy deck id=" + deckId));
        assertDeckEditable(deck);

        Flashcard card = Flashcard.builder()
                .deck(deck)
                .frontContent(form.getFrontContent())
                .backContent(form.getBackContent())
                .exampleSentence(form.getExampleSentence())
                .pronunciation(form.getPronunciation())
                .imageUrl(form.getImageUrl())
                .audioUrl(form.getAudioUrl())
                .build();

        return flashcardRepository.save(card);
    }

    @Transactional
    public Flashcard updateFlashcard(Integer flashcardId, FlashcardFormDTO form) {
        Flashcard card = getFlashcardById(flashcardId);
        assertDeckEditable(card.getDeck());
        card.setFrontContent(form.getFrontContent());
        card.setBackContent(form.getBackContent());
        card.setExampleSentence(form.getExampleSentence());
        card.setPronunciation(form.getPronunciation());
        card.setImageUrl(form.getImageUrl());
        card.setAudioUrl(form.getAudioUrl());
        return flashcardRepository.save(card);
    }

    @Transactional
    public void deleteFlashcard(Integer flashcardId) {
        Flashcard card = getFlashcardById(flashcardId);
        assertDeckEditable(card.getDeck());
        flashcardRepository.delete(card);
    }

    private void assertDeckEditable(Deck deck) {
        if (deck.getStatus() == DeckStatus.PENDING) {
            throw new RuntimeException("Deck đang chờ duyệt, bạn không thể chỉnh sửa lúc này.");
        }
    }
}
