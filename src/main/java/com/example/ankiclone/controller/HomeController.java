package com.example.ankiclone.controller;

import com.example.ankiclone.config.DemoUserConfig;
import com.example.ankiclone.dto.DeckSummaryDTO;
import com.example.ankiclone.service.DeckService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final DeckService deckService;
    private final DemoUserConfig demoUser;

    /**
     * GET /
     * Trang chủ: hiển thị danh sách deck của user (yêu cầu đã đăng nhập)
     */
    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        Object sessionUserId = session != null ? session.getAttribute(AuthController.SESSION_USER_ID) : null;
        if (!(sessionUserId instanceof Integer)) {
            return "redirect:/login";
        }

        Integer userId = (Integer) sessionUserId;
        List<DeckSummaryDTO> decks = deckService.getDeckSummariesForUser(userId);

        model.addAttribute("decks", decks);
        model.addAttribute("userId", userId);
        model.addAttribute("username", session.getAttribute(AuthController.SESSION_USERNAME));
        return "index"; // templates/index.html
    }
}
