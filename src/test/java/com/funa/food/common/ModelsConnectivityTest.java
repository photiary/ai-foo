package com.funa.food.common;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Connectivity smoke test for local OpenAI-compatible server (e.g., LM Studio).
 * Verifies that http://localhost:1234/v1/models is reachable.
 *
 * This test is marked as a smoke/integration style check and will be skipped if the endpoint is not running
 * to avoid breaking unit test pipelines where LM Studio is not available.
 */
public class ModelsConnectivityTest {

    private static final String MODELS_URL = System.getProperty("OPENAI_MODELS_URL", "http://localhost:1234/v1/models");

    @Test
    void can_connect_to_local_models_endpoint() throws IOException {
        try {
            URL url = URI.create(MODELS_URL).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            conn.connect();
            int code = conn.getResponseCode();
            // Accept any HTTP response code < 500 as a sign of reachability (e.g., 200/401/404 are fine)
            boolean reachable = code < 500;
            Assumptions.assumeTrue(reachable, () -> "Skipping: models endpoint not reachable, HTTP code=" + code);
            assertTrue(reachable);
        } catch (IOException ex) {
            // Skip test when not reachable to keep CI green without local server
            Assumptions.assumeTrue(false, "Skipping: cannot connect to http://localhost:1234/v1/models - " + ex.getMessage());
        }
    }
}
