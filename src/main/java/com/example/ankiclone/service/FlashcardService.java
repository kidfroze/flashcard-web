package com.example.ankiclone.service;

import com.example.ankiclone.dto.FlashcardFormDTO;
import com.example.ankiclone.model.Deck;
import com.example.ankiclone.model.Flashcard;
import com.example.ankiclone.repository.DeckRepository;
import com.example.ankiclone.repository.FlashcardRepository;
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
        flashcardRepository.deleteById(flashcardId);
    }
}
