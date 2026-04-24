package com.example.playlist.game.dto;

public record QuizAnswerResponse(
        boolean correct,
        boolean titleCorrect,
        boolean artistCorrect,
        String title,
        String artist,
        String albumImageUrl
) {}
