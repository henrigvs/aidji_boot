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

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps Aidji Discovery properties to Spring Cloud Eureka properties.
 */
public class AidjiDiscoveryEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String enabled = environment.getProperty("aidji.discovery.enabled", "true");
        if (!"true".equalsIgnoreCase(enabled)) {
            return;
        }

        Map<String, Object> properties = new HashMap<>();

        // Map server URL
        String serverUrl = environment.getProperty("aidji.discovery.eureka.server-url");
        String serverUrls = environment.getProperty("aidji.discovery.eureka.server-urls");

        if (serverUrls != null && !serverUrls.isBlank()) {
            properties.put("eureka.client.service-url.defaultZone", serverUrls);
        } else if (serverUrl != null && !serverUrl.isBlank()) {
            properties.put("eureka.client.service-url.defaultZone", serverUrl);
        }

        // Map instance properties
        String preferIpAddress = environment.getProperty("aidji.discovery.eureka.instance.prefer-ip-address", "true");
        properties.put("eureka.instance.prefer-ip-address", preferIpAddress);

        String leaseRenewal = environment.getProperty("aidji.discovery.eureka.instance.lease-renewal-interval-seconds", "10");
        properties.put("eureka.instance.lease-renewal-interval-in-seconds", leaseRenewal);

        String leaseExpiration = environment.getProperty("aidji.discovery.eureka.instance.lease-expiration-duration-seconds", "30");
        properties.put("eureka.instance.lease-expiration-duration-in-seconds", leaseExpiration);

        String hostname = environment.getProperty("aidji.discovery.eureka.instance.hostname");
        if (hostname != null && !hostname.isBlank()) {
            properties.put("eureka.instance.hostname", hostname);
        }

        // Enable Eureka client
        properties.put("eureka.client.register-with-eureka", "true");
        properties.put("eureka.client.fetch-registry", "true");

        // Add as a low-priority property source (can be overridden)
        environment.getPropertySources().addLast(
                new MapPropertySource("aidjiDiscoveryProperties", properties)
        );
    }
}