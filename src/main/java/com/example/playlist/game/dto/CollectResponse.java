package com.example.playlist.game.dto;

public record CollectResponse(
        String decade,
        int newlyCollected,
        int totalInDb
) {}
