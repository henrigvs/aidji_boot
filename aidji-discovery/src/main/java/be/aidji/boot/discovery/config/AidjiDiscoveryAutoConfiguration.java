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
package be.aidji.boot.discovery.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Auto-configuration for Aidji Discovery with Eureka.
 *
 * <p>This configuration maps Aidji properties to Spring Cloud Eureka properties.</p>
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(name = "com.netflix.discovery.EurekaClient")
@ConditionalOnProperty(name = "aidji.discovery.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AidjiDiscoveryProperties.class)
public class AidjiDiscoveryAutoConfiguration {

    private final AidjiDiscoveryProperties properties;

    public AidjiDiscoveryAutoConfiguration(AidjiDiscoveryProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void logConfiguration() {
        log.info("Aidji Discovery enabled with Eureka server: {}",
                properties.eureka().getServiceUrlsAsString());
    }
}