package com.example.ankiclone.controller;

import com.example.ankiclone.config.DemoUserConfig;
import com.example.ankiclone.controller.AuthController;
import com.example.ankiclone.dto.DeckSummaryDTO;
import com.example.ankiclone.model.Deck;
import com.example.ankiclone.model.Flashcard;
import com.example.ankiclone.service.DeckService;
import com.example.ankiclone.service.FlashcardService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/public-decks")
@RequiredArgsConstructor
public class PublicDeckController {

    private final DeckService deckService;
    private final FlashcardService flashcardService;
    private final DemoUserConfig demoUser;

    @GetMapping
    public String listPublicDecks(Model model) {
        Integer userId = demoUser.getCurrentUserId();
        List<DeckSummaryDTO> publicDecks = deckService.getPublicDecks(userId);
        model.addAttribute("publicDecks", publicDecks);
        return "public-deck/list";
    }

    @GetMapping("/{deckId}")
    public String viewPublicDeck(@PathVariable Integer deckId, Model model) {
        Deck deck = deckService.getPublicDeckById(deckId);
        List<Flashcard> flashcards = flashcardService.getFlashcardsByDeck(deckId);
        model.addAttribute("deck", deck);
        model.addAttribute("flashcards", flashcards);
        return "public-deck/detail";
    }

    @PostMapping("/{deckId}/import")
    public String importPublicDeck(@PathVariable Integer deckId, RedirectAttributes redirectAttributes) {
        Integer userId = demoUser.getCurrentUserId();
        Deck imported = deckService.importPublicDeck(deckId, userId);
        redirectAttributes.addFlashAttribute("success",
                "Đã import deck thành công! Bạn có thể chỉnh sửa deck mới của mình.");
        return "redirect:/decks/" + imported.getDeckId();
    }

    @PostMapping("/{deckId}/hide")
    public String hidePublicDeck(@PathVariable Integer deckId, HttpSession session,
            RedirectAttributes redirectAttributes) {
        String role = (String) session.getAttribute(AuthController.SESSION_ROLE);
        if (!"admin".equals(role)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền thực hiện hành động này.");
            return "redirect:/public-decks";
        }
        deckService.hidePublicDeck(deckId);
        redirectAttributes.addFlashAttribute("success", "Đã ẩn deck khỏi danh sách công khai.");
        return "redirect:/public-decks";
    }
}
