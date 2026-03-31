package com.example.ankiclone.controller;

import com.example.ankiclone.config.DemoUserConfig;
import com.example.ankiclone.dto.DeckFormDTO;
import com.example.ankiclone.dto.FlashcardFormDTO;
import com.example.ankiclone.model.Deck;
import com.example.ankiclone.model.Flashcard;
import com.example.ankiclone.service.DeckService;
import com.example.ankiclone.service.FlashcardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/decks")
@RequiredArgsConstructor
public class DeckController {

    private final DeckService deckService;
    private final FlashcardService flashcardService;
    private final DemoUserConfig demoUser;

    // =====================================================
    // DECK: Tạo mới
    // =====================================================

    /** GET /decks/new — Form tạo deck mới */
    @GetMapping("/new")
    public String newDeckForm(Model model) {
        model.addAttribute("deckForm", new DeckFormDTO());
        return "deck/form"; // templates/deck/form.html
    }

    /** POST /decks — Lưu deck mới */
    @PostMapping
    public String createDeck(@Valid @ModelAttribute("deckForm") DeckFormDTO form,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "deck/form";
        }
        Deck deck = deckService.createDeck(form, demoUser.getCurrentUserId());
        redirectAttributes.addFlashAttribute("success", "Tạo deck thành công!");
        return "redirect:/decks/" + deck.getDeckId();
    }

    // =====================================================
    // DECK: Xem chi tiết (danh sách flashcard)
    // =====================================================

    /** GET /decks/{deckId} — Xem các flashcard trong deck */
    @GetMapping("/{deckId}")
    public String viewDeck(@PathVariable Integer deckId, Model model) {
        Deck deck = deckService.getDeckById(deckId);
        List<Flashcard> flashcards = flashcardService.getFlashcardsByDeck(deckId);

        model.addAttribute("deck", deck);
        model.addAttribute("flashcards", flashcards);
        model.addAttribute("flashcardForm", new FlashcardFormDTO()); // form thêm card nhanh
        return "deck/detail"; // templates/deck/detail.html
    }

    // =====================================================
    // DECK: Sửa
    // =====================================================

    /** GET /decks/{deckId}/edit — Form sửa deck */
    @GetMapping("/{deckId}/edit")
    public String editDeckForm(@PathVariable Integer deckId, Model model) {
        Deck deck = deckService.getDeckById(deckId);
        DeckFormDTO form = new DeckFormDTO();
        form.setTitle(deck.getTitle());
        form.setDescription(deck.getDescription());
        form.setIsPublic(deck.getIsPublic());

        model.addAttribute("deck", deck);
        model.addAttribute("deckForm", form);
        return "deck/form";
    }

    /** POST /decks/{deckId}/edit — Lưu sửa deck */
    @PostMapping("/{deckId}/edit")
    public String updateDeck(@PathVariable Integer deckId,
                             @Valid @ModelAttribute("deckForm") DeckFormDTO form,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("deck", deckService.getDeckById(deckId));
            return "deck/form";
        }
        deckService.updateDeck(deckId, form);
        redirectAttributes.addFlashAttribute("success", "Cập nhật deck thành công!");
        return "redirect:/decks/" + deckId;
    }

    // =====================================================
    // DECK: Xóa
    // =====================================================

    /** POST /decks/{deckId}/delete — Xóa deck */
    @PostMapping("/{deckId}/delete")
    public String deleteDeck(@PathVariable Integer deckId,
                             RedirectAttributes redirectAttributes) {
        deckService.deleteDeck(deckId);
        redirectAttributes.addFlashAttribute("success", "Đã xóa deck.");
        return "redirect:/";
    }

    // =====================================================
    // FLASHCARD: Thêm vào deck
    // =====================================================

    /** POST /decks/{deckId}/flashcards — Thêm flashcard mới */
    @PostMapping("/{deckId}/flashcards")
    public String addFlashcard(@PathVariable Integer deckId,
                               @Valid @ModelAttribute("flashcardForm") FlashcardFormDTO form,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            Deck deck = deckService.getDeckById(deckId);
            List<Flashcard> flashcards = flashcardService.getFlashcardsByDeck(deckId);
            model.addAttribute("deck", deck);
            model.addAttribute("flashcards", flashcards);
            return "deck/detail";
        }
        flashcardService.createFlashcard(deckId, form);
        redirectAttributes.addFlashAttribute("success", "Thêm flashcard thành công!");
        return "redirect:/decks/" + deckId;
    }

    // =====================================================
    // FLASHCARD: Sửa
    // =====================================================

    /** GET /decks/{deckId}/flashcards/{flashcardId}/edit */
    @GetMapping("/{deckId}/flashcards/{flashcardId}/edit")
    public String editFlashcardForm(@PathVariable Integer deckId,
                                    @PathVariable Integer flashcardId,
                                    Model model) {
        Flashcard card = flashcardService.getFlashcardById(flashcardId);
        FlashcardFormDTO form = new FlashcardFormDTO();
        form.setFrontContent(card.getFrontContent());
        form.setBackContent(card.getBackContent());
        form.setExampleSentence(card.getExampleSentence());
        form.setPronunciation(card.getPronunciation());
        form.setImageUrl(card.getImageUrl());
        form.setAudioUrl(card.getAudioUrl());

        model.addAttribute("deckId", deckId);
        model.addAttribute("flashcardId", flashcardId);
        model.addAttribute("flashcardForm", form);
        return "deck/flashcard-form"; // templates/deck/flashcard-form.html
    }

    /** POST /decks/{deckId}/flashcards/{flashcardId}/edit */
    @PostMapping("/{deckId}/flashcards/{flashcardId}/edit")
    public String updateFlashcard(@PathVariable Integer deckId,
                                  @PathVariable Integer flashcardId,
                                  @Valid @ModelAttribute("flashcardForm") FlashcardFormDTO form,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("deckId", deckId);
            model.addAttribute("flashcardId", flashcardId);
            return "deck/flashcard-form";
        }
        flashcardService.updateFlashcard(flashcardId, form);
        redirectAttributes.addFlashAttribute("success", "Cập nhật flashcard thành công!");
        return "redirect:/decks/" + deckId;
    }

    // =====================================================
    // FLASHCARD: Xóa
    // =====================================================

    /** POST /decks/{deckId}/flashcards/{flashcardId}/delete */
    @PostMapping("/{deckId}/flashcards/{flashcardId}/delete")
    public String deleteFlashcard(@PathVariable Integer deckId,
                                  @PathVariable Integer flashcardId,
                                  RedirectAttributes redirectAttributes) {
        flashcardService.deleteFlashcard(flashcardId);
        redirectAttributes.addFlashAttribute("success", "Đã xóa flashcard.");
        return "redirect:/decks/" + deckId;
    }
}
