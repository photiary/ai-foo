# 음식 이미지 분석 개인 맞춤 서비스

# API 수정

## 음식 이미지 분석 요청 수정

수정 API: /v1/food/analysis

AS-IS: `application.properties`의 `spring.ai.openai.chat.options.model`를 사용
TO-BE: 사용자가 LLM 모델을 선택

- RequestBody 추가항목:
    - modelName: 필수

- API의 요청 파라미터를 Spring AI에 설정하여 Open AI에 요청을 한다.
- 요청 파라미터 `modelName` == 'local-model'인 경우 `LocalModelChatClientConfig`를 사용한다.
  - baseurl: `http://localhost:1234`
  - api-key: `dummy`

- 요청 파라미터 `modelName` != 'local-model'가 아닌 경우 디폴트 ChatClient를 사용한다.