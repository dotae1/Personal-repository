package com.example.playlist.game.dto;

public record CollectResponse(
        int decade,
        int newlyCollected,
        int totalInDb
) {}
