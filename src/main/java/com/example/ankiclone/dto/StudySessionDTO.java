package com.example.ankiclone.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudySessionDTO {
    private Integer deckId;
    private String deckTitle;
    private Integer flashcardId;
    private String frontContent;
    private String backContent;
    private String exampleSentence;
    private String pronunciation;
    private String imageUrl;
    private String audioUrl;
    private int remainingCards;   // số card còn lại trong session
    private int totalDue;         // tổng số card cần ôn
    private boolean showAnswer;   // đang hiển thị mặt trước hay sau
}
