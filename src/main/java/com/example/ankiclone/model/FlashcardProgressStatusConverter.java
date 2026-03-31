package com.example.ankiclone.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class FlashcardProgressStatusConverter implements AttributeConverter<FlashcardProgress.Status, String> {

    @Override
    public String convertToDatabaseColumn(FlashcardProgress.Status attribute) {
        if (attribute == null) return null;
        return attribute.getValue(); // "new" / "learning" / "review" / "mastered"
    }

    @Override
    public FlashcardProgress.Status convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        String normalized = dbData.trim().toLowerCase();
        for (FlashcardProgress.Status s : FlashcardProgress.Status.values()) {
            if (s.getValue().equals(normalized)) return s;
            if (s.name().equalsIgnoreCase(normalized)) return s; // tolerate "LEARNING" etc
        }
        throw new IllegalArgumentException("Unknown FlashcardProgress.Status value: " + dbData);
    }
}
