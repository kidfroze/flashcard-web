package com.example.ankard.dto;

import com.example.ankard.model.ReviewHistory;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ReviewResultDTO {
    private Integer flashcardId;
    private Integer deckId;
    private ReviewHistory.ReviewResult result; // again | hard | good | easy
}
