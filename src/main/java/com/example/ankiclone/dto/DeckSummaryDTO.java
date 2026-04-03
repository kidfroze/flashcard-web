package com.example.ankiclone.dto;

import com.example.ankiclone.model.DeckStatus;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeckSummaryDTO {
    private Integer deckId;
    private String title;
    private String description;
    private DeckStatus status;
    private long totalCards;
    private long newCards;
    private long learningCards;
    private long reviewCards;
}
