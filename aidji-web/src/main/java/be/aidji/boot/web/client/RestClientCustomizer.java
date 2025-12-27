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

import org.springframework.web.client.RestClient;

/**
 * Callback interface for customizing the RestClient.Builder used by Aidji.
 * 
 * <p>Implement this interface and register as a bean to add custom configuration
 * to all RestClient instances created by {@link AidjiRestClientFactory}.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * @Bean
 * public RestClientCustomizer authCustomizer() {
 *     return builder -> builder.defaultHeader("X-Api-Key", apiKey);
 * }
 * }</pre>
 */
@FunctionalInterface
public interface RestClientCustomizer {

    /**
     * Customize the RestClient.Builder.
     *
     * @param builder the builder to customize
     */
    void customize(RestClient.Builder builder);
}
