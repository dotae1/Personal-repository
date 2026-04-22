package com.example.playlist.game.service;

import com.example.playlist.game.dto.QuizTrackResponse;
import com.example.playlist.game.entity.QuizTrack;
import com.example.playlist.game.repository.QuizTrackMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final QuizTrackMapper quizTrackMapper;

    public QuizTrackResponse getQuizTrack(int decade) {
        QuizTrack track = quizTrackMapper.findRandomByDecade(decade);
        if (track == null) {
            throw new IllegalStateException("해당 연대의 트랙이 없습니다. 관리자 페이지에서 곡을 수집해주세요.");
        }
        log.info("[Game] 퀴즈 트랙 반환 - decade={}, title={}, artist={}", decade, track.getTitle(), track.getArtist());
        return QuizTrackResponse.fromEntity(track);
    }
}
