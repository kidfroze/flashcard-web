package com.example.ankiclone.controller;

import com.example.ankiclone.config.DemoUserConfig;
import com.example.ankiclone.dto.DeckSummaryDTO;
import com.example.ankiclone.service.DeckService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final DeckService deckService;
    private final DemoUserConfig demoUser;

    /**
     * GET /
     * Trang chủ: hiển thị danh sách deck của user
     */
    @GetMapping("/")
    public String home(Model model) {
        Integer userId = demoUser.getCurrentUserId();
        List<DeckSummaryDTO> decks = deckService.getDeckSummariesForUser(userId);

        model.addAttribute("decks", decks);
        model.addAttribute("userId", userId);
        return "index"; // templates/index.html
    }
}
