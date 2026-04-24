package com.example.playlist.game.dto;

public record QuizAnswerRequest(
        String quizId,
        String titleAnswer,
        String artistAnswer
) {}
