/*
 * Copyright 2025 Henri GEVENOIS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.aidji.boot.web.client;

import be.aidji.boot.core.exception.CommonErrorCode;
import be.aidji.boot.core.exception.TechnicalException;
import be.aidji.boot.web.AidjiWebProperties.RestClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.URI;

/**
 * Factory for creating pre-configured RestClient instances.
 * 
 * <p>Provides RestClient with:</p>
 * <ul>
 *   <li>Configured timeouts (connect, read)</li>
 *   <li>Request/response logging</li>
 *   <li>Error handling that converts to TechnicalException</li>
 *   <li>Trace ID propagation</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @Bean
 * public RestClient paymentServiceClient(AidjiRestClientFactory factory) {
 *     return factory.builder()
 *         .baseUrl("https://payment-service")
 *         .defaultHeader("X-Service-Name", "my-service")
 *         .build();
 * }
 * }</pre>
 */
public class AidjiRestClientFactory {

    private static final Logger log = LoggerFactory.getLogger(AidjiRestClientFactory.class);

    private final RestClientProperties config;
    private final RestClient.Builder baseBuilder;

    public AidjiRestClientFactory(RestClientProperties config, RestClient.Builder baseBuilder) {
        this.config = config;
        this.baseBuilder = baseBuilder;
    }

    /**
     * Creates a new RestClient.Builder with Aidji configuration.
     */
    public RestClient.Builder builder() {
        return baseBuilder.clone()
                .requestFactory(createRequestFactory())
                .requestInterceptor(new TraceIdInterceptor())
                .defaultStatusHandler(new AidjiResponseErrorHandler());
    }

    /**
     * Creates a RestClient with the given base URL.
     */
    public RestClient create(String baseUrl) {
        return builder().baseUrl(baseUrl).build();
    }

    private ClientHttpRequestFactory createRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(config.connectTimeout());
        factory.setReadTimeout(config.readTimeout());
        return factory;
    }

    /**
     * Interceptor that propagates trace ID from MDC to outgoing requests.
     */
    private static class TraceIdInterceptor implements org.springframework.http.client.ClientHttpRequestInterceptor {
        
        private static final String TRACE_ID_HEADER = "X-Trace-Id";

        @Override
        public org.springframework.http.client.ClientHttpResponse intercept(
                org.springframework.http.HttpRequest request,
                byte[] body,
                org.springframework.http.client.ClientHttpRequestExecution execution) throws IOException {
            
            String traceId = org.slf4j.MDC.get("traceId");
            if (traceId != null && !request.getHeaders().containsHeader(TRACE_ID_HEADER)) {
                request.getHeaders().add(TRACE_ID_HEADER, traceId);
            }
            
            return execution.execute(request, body);
        }
    }

    /**
     * Error handler that converts HTTP errors to TechnicalException.
     */
    private static class AidjiResponseErrorHandler implements ResponseErrorHandler {

        @Override
        public boolean hasError(org.springframework.http.client.ClientHttpResponse response) throws IOException {
            return response.getStatusCode().isError();
        }

        @Override
        public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
            int statusCode = response.getStatusCode().value();
            String statusText = response.getStatusText();

            CommonErrorCode errorCode = switch (statusCode) {
                case 404 -> CommonErrorCode.NOT_FOUND;
                case 401 -> CommonErrorCode.UNAUTHORIZED;
                case 403 -> CommonErrorCode.FORBIDDEN;
                case 502, 503, 504 -> CommonErrorCode.SERVICE_UNAVAILABLE;
                default -> statusCode >= 500 
                        ? CommonErrorCode.EXTERNAL_SERVICE_ERROR 
                        : CommonErrorCode.BAD_REQUEST;
            };

            throw TechnicalException.builder(errorCode)
                    .message("HTTP %d: %s", statusCode, statusText)
                    .context("httpStatus", statusCode)
                    .build();
        }
    }
}
