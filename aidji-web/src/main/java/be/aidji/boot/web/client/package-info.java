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

/**
 * HTTP client configuration and factory for REST communication.
 *
 * <p>This package provides pre-configured {@link org.springframework.web.client.RestClient} instances:</p>
 *
 * <ul>
 *   <li>{@link be.aidji.boot.web.client.AidjiRestClientFactory} - Factory for creating RestClient instances
 *       with sensible defaults (timeouts, error handling, logging)</li>
 *   <li>{@link be.aidji.boot.web.client.RestClientCustomizer} - Functional interface for customizing
 *       RestClient.Builder configuration</li>
 * </ul>
 *
 * <h2>Basic Usage</h2>
 * <pre>{@code
 * @Service
 * public class ExternalApiService {
 *
 *     private final RestClient restClient;
 *
 *     public ExternalApiService(AidjiRestClientFactory clientFactory) {
 *         this.restClient = clientFactory.create("https://api.example.com");
 *     }
 *
 *     public UserDto getUser(Long id) {
 *         return restClient.get()
 *             .uri("/users/{id}", id)
 *             .retrieve()
 *             .body(UserDto.class);
 *     }
 * }
 * }</pre>
 *
 * <h2>Customization</h2>
 * <pre>{@code
 * @Bean
 * public RestClientCustomizer authHeaderCustomizer() {
 *     return builder -> builder
 *         .defaultHeader("X-API-Key", apiKey)
 *         .requestInterceptor((request, body, execution) -> {
 *             // Add custom interceptor logic
 *             return execution.execute(request, body);
 *         });
 * }
 * }</pre>
 *
 * @see be.aidji.boot.web.client.AidjiRestClientFactory
 * @see be.aidji.boot.web.client.RestClientCustomizer
 * @see org.springframework.web.client.RestClient
 */
package be.aidji.boot.web.client;