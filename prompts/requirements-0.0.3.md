# 음식 이미지 분석 개인 맞춤 서비스

# API 수정

## 음식 이미지 분석 요청 수정

수정 API: /v1/food/analysis

AS-IS: `application.properties`의 `spring.ai.openai.chat.options.model`를 사용
TO-BE: 사용자가 LLM 모델을 선택

- RequestBody 추가항목:
    - modelName:

- API의 요청 파라미터를 Spring AI에 설정하여 Open AI에 요청을 한다.
- 요청 파라미터 `modelName`에 따른 `baseurl`
  - `modelName` = 'local-model' 인 경우는 baseurl를 'http://localhost:1234'로 접속한다.
  - `modelName` != 'local-model' 인 경우는 Spring AI의 디폴트로 Open AI에 접속한다.
- 요청 파라미터 `modelName`에 따른 `api-key`
  - `modelName` = 'local-model' 인 경우는 'dummy'
  - `modelName` != 'local-model' 인 경우는 spring.ai.openai.api-key에 정의된 키를 사용