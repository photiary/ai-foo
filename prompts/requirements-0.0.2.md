# 음식 이미지 분석 개인 맞춤 서비스

# API 수정

## 음식 이미지 분석 요청 수정

수정 API: /v1/food/analysis

AS-IS: 'cached_tokens' 미사용
TO-BE: Open AI의 'cached_tokens' 기능을 활용

- `UsageToken`에 'cachedTokens'을 추가
- 다음 코드를 사용하여 Open AI 응답에서 cachedTokens을 받는다.

```
Object nativeUsage = usage.getNativeUsage();
if (nativeUsage instanceof org.springframework.ai.openai.api.OpenAiApi.Usage openAiUsage) {
    Integer cachedTokens = openAiUsage.promptTokensDetails().cachedTokens();
}
```

- `OpenAiPricing`클래스에 `OpenAI-Pricing.md`를 참고로 Cached input 과금 정책을 설정한다.
- `cachedInputCost`를 `FoodAnalysisResponse`에 추가한다.
- Cached input 과금 정책에 따라 'inputCost', 'cachedInputCost', 'outputCost', 'totalCost'를 다시 계산한다.
  - 다음은 과금 계산식이다.
  ```
  inputCost = (promptTokens - cachedTokens) * 토큰당 Input 가격
  cachedInputCost = cachedTokens * 토큰당 Cached input 가격
  outputCost = completionTokens * 토큰당 Output 가격
  totalCost = inputCost + outputCost + cachedInputCost
  ```

- Database migration
  - UsageToken에 cachedTokens 추가로 마이그레이션 작업을 한다.
  - 기존에 있는 데이터는 0으로 업데이트를 한다.