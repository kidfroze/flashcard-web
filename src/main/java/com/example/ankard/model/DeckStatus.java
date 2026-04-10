package com.example.ankard.model;

public enum DeckStatus {
    PRIVATE,
    PENDING,
    APPROVED,
    REJECTED,
    // Legacy statuses kept for backward compatibility with old DB data.
    UNSHARE_PENDING,
    UNSHARE_PENDI
}
