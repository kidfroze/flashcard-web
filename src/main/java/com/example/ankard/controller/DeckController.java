package com.example.ankard.controller;

import com.example.ankard.config.DemoUserConfig;
import com.example.ankard.dto.DeckFormDTO;
import com.example.ankard.dto.FlashcardFormDTO;
import com.example.ankard.model.Deck;
import com.example.ankard.model.Flashcard;
import com.example.ankard.service.DeckService;
import com.example.ankard.service.FlashcardService;
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
        try {
            deckService.ensureDeckEditable(deckId);
        } catch (RuntimeException ex) {
            model.addAttribute("deck", deck);
            model.addAttribute("flashcards", flashcardService.getFlashcardsByDeck(deckId));
            model.addAttribute("flashcardForm", new FlashcardFormDTO());
            model.addAttribute("error", ex.getMessage());
            return "deck/detail";
        }
        DeckFormDTO form = new DeckFormDTO();
        form.setTitle(deck.getTitle());
        form.setDescription(deck.getDescription());

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
        try {
            deckService.updateDeck(deckId, form);
            redirectAttributes.addFlashAttribute("success", "Cập nhật deck thành công!");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/decks/" + deckId;
    }

    @PostMapping("/{deckId}/submit-share")
    public String submitShare(@PathVariable Integer deckId, RedirectAttributes redirectAttributes) {
        try {
            deckService.submitDeckForReview(deckId, demoUser.getCurrentUserId());
            redirectAttributes.addFlashAttribute("success", "Đã gửi deck để admin duyệt chia sẻ.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/decks/" + deckId;
    }

    @PostMapping("/{deckId}/cancel-share-review")
    public String cancelShareReview(@PathVariable Integer deckId, RedirectAttributes redirectAttributes) {
        try {
            deckService.cancelDeckReview(deckId, demoUser.getCurrentUserId());
            redirectAttributes.addFlashAttribute("success", "Đã hủy yêu cầu duyệt chia sẻ.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/decks/" + deckId;
    }

    @PostMapping("/{deckId}/stop-share")
    public String stopShare(@PathVariable Integer deckId, RedirectAttributes redirectAttributes) {
        try {
            deckService.stopSharingDeck(deckId, demoUser.getCurrentUserId());
            redirectAttributes.addFlashAttribute("success", "Đã ngừng chia sẻ deck.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/decks/" + deckId;
    }

    // =====================================================
    // DECK: Xóa
    // =====================================================

    /** POST /decks/{deckId}/delete — Xóa deck */
    @PostMapping("/{deckId}/delete")
    public String deleteDeck(@PathVariable Integer deckId,
                             RedirectAttributes redirectAttributes) {
        try {
            deckService.deleteDeck(deckId);
            redirectAttributes.addFlashAttribute("success", "Đã xóa deck.");
            return "redirect:/";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/decks/" + deckId;
        }
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
        try {
            flashcardService.createFlashcard(deckId, form);
            redirectAttributes.addFlashAttribute("success", "Thêm flashcard thành công!");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
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
        deckService.ensureDeckEditable(deckId);
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
        try {
            flashcardService.updateFlashcard(flashcardId, form);
            redirectAttributes.addFlashAttribute("success", "Cập nhật flashcard thành công!");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
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
        try {
            flashcardService.deleteFlashcard(flashcardId);
            redirectAttributes.addFlashAttribute("success", "Đã xóa flashcard.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/decks/" + deckId;
    }
}
