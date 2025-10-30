# Open AI LLM을 활용한 식사 이미지 분석

- 사용자의 상태와 식사이미지를 분석하여, 식사 정보(이름, 이미지상 위치, 칼로리, 탄단지)와 AI 시단 제안을 제공
- 이미지 분석에 사용한 Token 사용량을 제공

## 🥚 Open AI or Local LLM

- Open AI: Open AI API Key를 발급하여 사용
- Local LLM: LM Studio 설치하여 사용

## 🍕 주요 라이브러리

- Spring AI
- Spring Data JPA
- Flyway

## 🍔 주요 리소스 구조

``` 
root
├─ .junie                    # AI-Agent 가이드라인 프롬프트
├─ prompts                   # 기능 프롬프트
├─ src.main.java.com.funa
│  ├─ food                   # 식사 이미지 분석
│  └─ usage                  # 토큰 사용량
└─ src.main.resources
   └─ db.migration           # DB 마이그레이션
```

## 🍟 Database

- 시작

`docker compose -f compose.yaml -p ai-foo up -d postgres`

- 재시작

`docker compose -f compose.yaml -p ai-foo restart postgres`

## 🌭 Swagger

http://localhost:8080/swagger-ui/index.html

http://localhost:8080/api-docs (Front-end에서 AI Prompt로 사용)
