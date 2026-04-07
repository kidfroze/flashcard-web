package com.example.ankard.model;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Entity
@Table(name = "flashcard_tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(FlashcardTag.FlashcardTagId.class)
public class FlashcardTag {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flashcard_id")
    private Flashcard flashcard;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlashcardTagId implements Serializable {
        private Integer flashcard;
        private Integer tag;
    }
}
