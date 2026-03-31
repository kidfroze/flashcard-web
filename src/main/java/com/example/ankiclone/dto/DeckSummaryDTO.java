package com.example.ankiclone.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeckSummaryDTO {
    private Integer deckId;
    private String title;
    private String description;
    private Boolean isPublic;
    private long totalCards;
    private long newCards;
    private long learningCards;
    private long reviewCards;
}
