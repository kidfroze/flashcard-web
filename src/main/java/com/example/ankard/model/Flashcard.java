package com.example.ankard.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "flashcards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flashcard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flashcard_id")
    private Integer flashcardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id", nullable = false)
    @JsonIgnore
    private Deck deck;

    @Column(name = "front_content", nullable = false, columnDefinition = "TEXT")
    private String frontContent;

    @Column(name = "back_content", nullable = false, columnDefinition = "TEXT")
    private String backContent;

    @Column(name = "example_sentence", columnDefinition = "TEXT")
    private String exampleSentence;

    @Column(name = "pronunciation", length = 255)
    private String pronunciation;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "audio_url", length = 255)
    private String audioUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "flashcard", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<FlashcardTag> flashcardTags;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
