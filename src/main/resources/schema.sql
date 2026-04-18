CREATE TABLE IF NOT EXISTS member
(
    id               BIGSERIAL PRIMARY KEY,
    login_id         VARCHAR(50)  UNIQUE NOT NULL,
    email            VARCHAR(255) UNIQUE NOT NULL,
    password         VARCHAR(255),
    nickname         VARCHAR(50),
    name             VARCHAR(50)  NOT NULL,
    gender           VARCHAR(10),
    age              INTEGER,
    provider         VARCHAR(20)  DEFAULT 'LOCAL',
    profile_complete BOOLEAN      DEFAULT true,
    created_at       TIMESTAMP    DEFAULT NOW(),
    updated_at       TIMESTAMP    DEFAULT NOW(),
    is_deleted       BOOLEAN      DEFAULT false
);

-- 소셜 계정 연동 테이블 (한 회원이 여러 소셜 계정을 가질 수 있음)
CREATE TABLE IF NOT EXISTS member_social
(
    id          BIGSERIAL PRIMARY KEY,
    member_id   BIGINT       NOT NULL REFERENCES member (id) ON DELETE CASCADE,
    provider    VARCHAR(20)  NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP    DEFAULT NOW(),
    UNIQUE (provider, provider_id)
);

-- 기존 DB에 컬럼이 없는 경우를 위한 마이그레이션 (이미 테이블이 존재할 때)
-- 아래 DDL은 최초 실행 시 오류가 날 수 있으므로, 기존 DB라면 직접 실행하세요
-- ALTER TABLE member ADD COLUMN IF NOT EXISTS profile_complete BOOLEAN DEFAULT true;
-- ALTER TABLE member ALTER COLUMN password DROP NOT NULL;
-- ALTER TABLE member ALTER COLUMN nickname DROP NOT NULL;

CREATE TABLE IF NOT EXISTS playlist
(
    id                  BIGSERIAL PRIMARY KEY,
    member_id           BIGINT REFERENCES member (id) ON DELETE CASCADE,
    spotify_playlist_id VARCHAR(255),
    name                VARCHAR(255) NOT NULL,
    prompt              TEXT         NOT NULL,
    created_at          TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS song
(
    id                BIGSERIAL PRIMARY KEY,
    spotify_track_id  VARCHAR(255) UNIQUE NOT NULL,
    spotify_track_uri VARCHAR(255),
    title             VARCHAR(255) NOT NULL,
    artist            VARCHAR(255) NOT NULL,
    album             VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS playlist_song
(
    id          BIGSERIAL PRIMARY KEY,
    playlist_id BIGINT REFERENCES playlist (id) ON DELETE CASCADE,
    song_id     BIGINT REFERENCES song (id),
    position    INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS tag
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS playlist_tag
(
    playlist_id BIGINT REFERENCES playlist (id) ON DELETE CASCADE,
    tag_id      BIGINT REFERENCES tag (id) ON DELETE CASCADE,
    PRIMARY KEY (playlist_id, tag_id)
);