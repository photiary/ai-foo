# 음식 이미지 분석 개인 맞춤 서비스

# API

## 챗

- /v1/chat/completions
- post
- RequestBody:

  "content": string 요청 프롬프트

- Open API 호출 /v1/chat/completions
- LLM으로 부터 응답이 완료된 후에 AIP의 응답을 한다.

## 모델 정보

- /v1/models
- get

- Open API http://localhost:1234/v1/models 를 호출하여 응답 받은 정보를 응답


## 음식 이미지 분석 요청

- /vi/food/analysis
- 음식 이미지 업로드
- RequestBody:
  "status": string 사용자의 상태 `,`구분자로 상태표현 예) 남성,64세,체중78kg,키167cm,공복혈당120
- Open API에 음식이미지, RequestBody, 페르소나, 응답 json 스키마를 전달한다.
- 페르소나: 너는 사용자의 상태와 식사를 분석하여 건강한 삶을 유지하도록 서포트하는 영양사이다.

- 응답 스키마 json
    - 식사 이미지를 분석한 음식의 리스트
    - 음식별 탄단지, 칼로리, 이미지에서의 위치(x, y)
    - 사용자의 상태를 고려하여 적합한 식사 이미지를 분석하여 더 좋은 식사를 제안