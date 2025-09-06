package com.funa.food.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.HttpStatusCode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Interceptor to log OpenAI-compatible HTTP requests/responses in detail.
 * Requirements (per issue):
 *  - Print headers without masking (Authorization visible)
 *  - Print full request/response bodies (no truncation)
 */
public class OpenAiHttpLogging implements ClientHttpRequestInterceptor {
    private static final Logger log = LoggerFactory.getLogger(OpenAiHttpLogging.class);

    private final boolean enabled;

    public OpenAiHttpLogging(boolean enabled, int ignoredMaxBodyBytes) {
        this.enabled = enabled;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (!enabled) {
            return execution.execute(request, body);
        }
        String traceId = UUID.randomUUID().toString();
        long start = System.nanoTime();

        // Request line with unmasked headers and full body
        String requestHeaders = toHeaderString(request.getHeaders());
        String requestBodyText = new String(body, StandardCharsets.UTF_8);
        log.info("[openai.http] traceId={} direction=request method={} url={} headers={} bodyBytes={} body={}",
                traceId, request.getMethod(), request.getURI(), requestHeaders, body.length, requestBodyText);

        ClientHttpResponse response = execution.execute(request, body);
        long durationMs = (System.nanoTime() - start) / 1_000_000L;

        // Read full response body (no cap)
        byte[] responseCopy = readAll(response);
        String responseBodyText = new String(responseCopy, StandardCharsets.UTF_8);
        int status = response.getStatusCode().value();
        log.info("[openai.http] traceId={} direction=response status={} durationMs={} bodyBytes={} body={}",
                traceId, status, durationMs, responseCopy.length, responseBodyText);

        // Return a new response with the copied body so downstream can still read it
        return new BufferingClientHttpResponse(response, responseCopy);
    }

    private static String toHeaderString(HttpHeaders headers) {
        return headers.toSingleValueMap().toString();
    }

    private static byte[] readAll(ClientHttpResponse response) throws IOException {
        try (var in = response.getBody(); var bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int r;
            while ((r = in.read(buf)) != -1) {
                bos.write(buf, 0, r);
            }
            return bos.toByteArray();
        }
    }

    private static class BufferingClientHttpResponse implements ClientHttpResponse {
        private final ClientHttpResponse delegate;
        private final byte[] body;
        BufferingClientHttpResponse(ClientHttpResponse delegate, byte[] body) {
            this.delegate = delegate;
            this.body = body;
        }
        @Override public HttpStatusCode getStatusCode() throws IOException { return delegate.getStatusCode(); }
        @Override public String getStatusText() throws IOException { return delegate.getStatusText(); }
        @Override public void close() { delegate.close(); }
        @Override public org.springframework.http.HttpHeaders getHeaders() { return delegate.getHeaders(); }
        @Override public java.io.InputStream getBody() { return new ByteArrayInputStream(body); }
    }
}
