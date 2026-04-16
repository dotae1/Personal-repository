CREATE TABLE IF NOT EXISTS member
(
    id         BIGSERIAL PRIMARY KEY,
    login_id   VARCHAR(50)  UNIQUE NOT NULL,
    email      VARCHAR(255) UNIQUE NOT NULL,
    password   VARCHAR(255) NOT NULL,
    nickname   VARCHAR(50)  NOT NULL,
    name       VARCHAR(50)  NOT NULL,
    gender     VARCHAR(10),
    age        INTEGER,
    provider   VARCHAR(20)  DEFAULT 'LOCAL',
    created_at TIMESTAMP    DEFAULT NOW(),
    updated_at TIMESTAMP    DEFAULT NOW(),
    is_deleted BOOLEAN      DEFAULT false
);

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