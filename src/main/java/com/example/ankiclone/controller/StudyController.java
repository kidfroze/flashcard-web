package com.example.ankiclone.controller;

import com.example.ankiclone.config.DemoUserConfig;
import com.example.ankiclone.dto.StudySessionDTO;
import com.example.ankiclone.model.ReviewHistory;
import com.example.ankiclone.service.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/study")
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;
    private final DemoUserConfig demoUser;

    /**
     * GET /study/{deckId}
     * Bắt đầu/tiếp tục session học — hiển thị mặt trước flashcard
     */
    @GetMapping("/{deckId}")
    public String studyDeck(@PathVariable Integer deckId, Model model) {
        Integer userId = demoUser.getCurrentUserId();
        StudySessionDTO session = studyService.getNextCard(deckId, userId);

        if (session == null) {
            // Không còn card nào → hoàn thành
            model.addAttribute("deckId", deckId);
            return "study/complete"; // templates/study/complete.html
        }

        // Lấy nhãn dự kiến cho các nút (chỉ dùng khi show answer)
        String[] labels = studyService.getNextReviewLabels(session.getFlashcardId(), userId);

        model.addAttribute("studySession", session);
        model.addAttribute("showAnswer", false);
        model.addAttribute("labels", labels); // [again, hard, good, easy]
        return "study/study"; // templates/study/study.html
    }

    /**
     * POST /study/{deckId}/show-answer
     * Lật thẻ — hiển thị mặt sau và các nút đánh giá
     */
    @PostMapping("/{deckId}/show-answer")
    public String showAnswer(@PathVariable Integer deckId,
            @RequestParam Integer flashcardId,
            Model model) {
        Integer userId = demoUser.getCurrentUserId();
        StudySessionDTO session = studyService.getCardDetail(deckId, flashcardId, userId);

        String[] labels = studyService.getNextReviewLabels(flashcardId, userId);

        model.addAttribute("studySession", session);
        model.addAttribute("showAnswer", true);
        model.addAttribute("labels", labels);
        return "study/study";
    }

    /**
     * POST /study/{deckId}/review
     * Nhận kết quả đánh giá (again/hard/good/easy) → lưu → card tiếp theo
     */
    @PostMapping("/{deckId}/review")
    public String submitReview(@PathVariable Integer deckId,
            @RequestParam Integer flashcardId,
            @RequestParam String result,
            RedirectAttributes redirectAttributes) {
        Integer userId = demoUser.getCurrentUserId();

        try {
            ReviewHistory.ReviewResult reviewResult = ReviewHistory.ReviewResult.valueOf(result.toLowerCase());
            studyService.submitReview(flashcardId, userId, reviewResult);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Kết quả không hợp lệ: " + result);
        }

        // Redirect về trang học để lấy card tiếp theo (PRG pattern)
        return "redirect:/study/" + deckId;
    }
}
