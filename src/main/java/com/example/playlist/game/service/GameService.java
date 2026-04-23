package com.example.playlist.game.service;

import com.example.playlist.game.dto.QuizTrackResponse;
import com.example.playlist.game.entity.QuizTrack;
import com.example.playlist.game.repository.QuizTrackMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameService {

    private final QuizTrackMapper quizTrackMapper;

    public QuizTrackResponse getQuizTrack(String decade) {
        QuizTrack track = quizTrackMapper.findRandomByDecade(decade);
        if (track == null) {
            throw new IllegalStateException("해당 연대의 곡이 없습니다. 먼저 수집을 진행해주세요.");
        }
        return QuizTrackResponse.fromEntity(track);
    }
}
