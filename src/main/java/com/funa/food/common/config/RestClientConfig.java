package com.funa.food.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class RestClientConfig {

    private static final Logger log = LoggerFactory.getLogger(RestClientConfig.class);

    @Value("${openai.http.connect-timeout-ms:10000}")
    private long connectTimeoutMs;

    @Value("${openai.http.read-timeout-ms:300000}")
    private long readTimeoutMs;

    @Value("${openai.http.logging.enabled:true}")
    private boolean logEnabled;

    @Value("${openai.http.logging.max-body-bytes:4096}")
    private int logMaxBodyBytes;

    @Bean
    @ConditionalOnMissingBean
    public RestClientCustomizer defaultTimeoutRestClientCustomizer() {
        // Configure extended timeouts to accommodate large LLM responses / model warmup
        Duration connectTimeout = Duration.ofMillis(connectTimeoutMs);
        Duration readTimeout = Duration.ofMillis(readTimeoutMs);
        return (RestClient.Builder builder) -> {
            HttpClient httpClient = HttpClient.newBuilder()
                    // Force HTTP/1.1 to avoid h2c upgrade attempts that some local servers (e.g., LM Studio) mishandle
                    .version(HttpClient.Version.HTTP_1_1)
                    .connectTimeout(connectTimeout)
                    .build();
            JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
            requestFactory.setReadTimeout(readTimeout);
            builder.requestFactory(requestFactory);
            // Detailed OpenAI HTTP logging (masked auth, truncated bodies)
            builder.requestInterceptor(new OpenAiHttpLogging(logEnabled, logMaxBodyBytes));
            log.info("Configured RestClient timeouts: connect={}ms, read={}ms, loggingEnabled={}, maxBodyBytes={}",
                    connectTimeout.toMillis(), readTimeout.toMillis(), logEnabled, logMaxBodyBytes);
        };
    }
}
