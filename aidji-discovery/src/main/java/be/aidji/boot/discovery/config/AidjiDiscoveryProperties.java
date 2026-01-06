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

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configuration properties for Aidji Discovery module.
 *
 * <p>Example configuration:</p>
 * <pre>{@code
 * aidji:
 *   discovery:
 *     enabled: true
 *     eureka:
 *       server-url: http://localhost:8761/eureka
 *       # Or multiple servers:
 *       # server-urls:
 *       #   - http://eureka1:8761/eureka
 *       #   - http://eureka2:8761/eureka
 *       instance:
 *         prefer-ip-address: true
 *         lease-renewal-interval-seconds: 10
 *         lease-expiration-duration-seconds: 30
 * }</pre>
 *
 * <p>Environment variable support :</p>
 * <pre>{@code
 * aidji:
 *   discovery:
 *     eureka:
 *       server-url: ${EUREKA_SERVER_URL:http://localhost:8761/eureka}
 * }</pre>
 */
@ConfigurationProperties(prefix = "aidji.discovery")
public record AidjiDiscoveryProperties(
        boolean enabled,
        EurekaProperties eureka
) {
    public AidjiDiscoveryProperties {
        if (eureka == null) {
            eureka = new EurekaProperties(
                    "http://eureka:8761/eureka",
                    List.of(),
                    new InstanceProperties(true, 10, 30, null)
            );
        }
    }

    public record EurekaProperties(
            String serverUrl,
            List<String> serverUrls,
            InstanceProperties instance
    ) {
        public EurekaProperties {
            if (serverUrl == null && (serverUrls == null || serverUrls.isEmpty())) {
                serverUrl = "http://eureka:8761/eureka";
            }
            if (instance == null) {
                instance = new InstanceProperties(true, 10, 30, null);
            }
        }

        /**
         * Returns the service URLs as a comma-separated string for Eureka configuration.
         */
        public String getServiceUrlsAsString() {
            if (serverUrls != null && !serverUrls.isEmpty()) {
                return String.join(",", serverUrls);
            }
            return serverUrl;
        }
    }

    public record InstanceProperties(
            boolean preferIpAddress,
            int leaseRenewalIntervalSeconds,
            int leaseExpirationDurationSeconds,
            String hostname
    ) {
        public InstanceProperties {
            if (leaseRenewalIntervalSeconds <= 0) {
                leaseRenewalIntervalSeconds = 10;
            }
            if (leaseExpirationDurationSeconds <= 0) {
                leaseExpirationDurationSeconds = 30;
            }
        }
    }
}