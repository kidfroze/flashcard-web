package com.example.ankard.controller;

import com.example.ankard.model.Deck;
import com.example.ankard.service.DeckService;
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
@RequestMapping("/admin/decks")
@RequiredArgsConstructor
public class AdminDeckController {

    private final DeckService deckService;

    @GetMapping("/pending")
    public String pendingDecks(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập trang admin.");
            return "redirect:/";
        }
        List<Deck> pendingDecks = deckService.getPendingDecks();
        model.addAttribute("pendingDecks", pendingDecks);
        return "admin/deck-pending";
    }

    @PostMapping("/{deckId}/approve")
    public String approve(@PathVariable Integer deckId, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền thực hiện hành động này.");
            return "redirect:/";
        }
        deckService.approveDeck(deckId);
        redirectAttributes.addFlashAttribute("success", "Đã duyệt deck.");
        return "redirect:/admin/decks/pending";
    }

    @PostMapping("/{deckId}/reject")
    public String reject(@PathVariable Integer deckId, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền thực hiện hành động này.");
            return "redirect:/";
        }
        deckService.rejectDeck(deckId);
        redirectAttributes.addFlashAttribute("success", "Đã từ chối deck.");
        return "redirect:/admin/decks/pending";
    }

    private boolean isAdmin(HttpSession session) {
        String role = (String) session.getAttribute(AuthController.SESSION_ROLE);
        return role != null && "admin".equalsIgnoreCase(role);
    }
}
