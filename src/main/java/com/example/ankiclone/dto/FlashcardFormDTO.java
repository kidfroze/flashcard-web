package com.example.ankiclone.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FlashcardFormDTO {

    @NotBlank(message = "Mặt trước không được để trống")
    private String frontContent;

    @NotBlank(message = "Mặt sau không được để trống")
    private String backContent;

    private String exampleSentence;
    private String pronunciation;
    private String imageUrl;
    private String audioUrl;
}
