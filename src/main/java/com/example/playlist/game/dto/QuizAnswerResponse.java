package com.example.playlist.game.dto;

public record QuizAnswerResponse(
        boolean correct,
        String title,
        String artist,
        String albumImageUrl
) {}
