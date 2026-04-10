 PR 제목:
  feat: Gemini AI 플레이리스트 추천 기능 및 프로젝트 기반 구조 구현

  PR 본문:
  ## 📌 개요
  Gemini AI를 활용한 플레이리스트 추천 기능과 프로젝트 공통 기반 구조를 구현했습니다.

  ---

  ## ✅ 주요 변경사항

  ### 🤖 Gemini AI 플레이리스트 추천
  - `POST /api/v1/gemini/playlist` 엔드포인트 구현
  - `GeminiConfig`: temperature(0.3), topP(0.8), responseSchema로 JSON 구조화 응답 강제
  - `GeminiService`: Gemini API 호출 및 응답 파싱
  - **버그 수정**: `GeminiRequest`에 `@Getter/@Setter` 누락으로 `prompt`가 항상 `null`로 전달되던 문제 수정

  ### 🗂 도메인 엔티티
  - `Member`, `Playlist`, `Song`, `Tag`, `PlaylistSong`, `PlaylistTag`

  ### 🌐 전역 공통 구조
  - `ErrorCode` 인터페이스 + `GlobalErrorCode`, `GeminiErrorCode`
  - `GlobalExceptionHandler` - 전역 예외 처리
  - `ErrorResponse`, `SuccessResponse`, `SuccessCode` - 응답 포맷 통일

  ### 🗄 DB 및 설정
  - `schema.sql` - 테이블 정의
  - MyBatis 매퍼 (`MemberMapper.xml`, `PlaylistMapper.xml`)
  - `application.yml` - MyBatis, Gemini API Key, Swagger 서버 URL 설정

  ### 📦 의존성 추가 (`build.gradle`)
  - `com.google.genai:google-genai:1.0.0`
  - `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.2`
  - `spring-boot-starter-webflux`
  - `me.paulschwarz:spring-dotenv:4.0.0`

  ---

  ## 🔑 환경변수 (.env 필요)
  DATABASE_URL=jdbc:postgresql://localhost:5432/
  DATABASE_USERNAME=
  DATABASE_PASSWORD=
  GEMINI_API_KEY=

  ---

  ## 🧪 테스트 방법
  ```bash
  ./gradlew bootRun

  curl -X POST http://localhost:8080/api/v1/gemini/playlist \
    -H "Content-Type: application/json" \
    -d '{"prompt": "오늘 학교 축제인데 제일 신나는 노래 추천해줘"}'

  ---
