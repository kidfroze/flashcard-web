package com.example.ankiclone.dto;

import com.example.ankiclone.model.ReviewHistory;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ReviewResultDTO {
    private Integer flashcardId;
    private Integer deckId;
    private ReviewHistory.ReviewResult result; // again | hard | good | easy
}
