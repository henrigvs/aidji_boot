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
 * Aidji Discovery module for service discovery with Eureka.
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>Auto-configured Eureka client</li>
 *   <li>Environment variable support for server URL</li>
 *   <li>Sensible defaults for development</li>
 *   <li>Multiple Eureka server support (HA)</li>
 * </ul>
 *
 * <h2>Quick Start</h2>
 * <pre>{@code
 * # application.yml
 * aidji:
 *   discovery:
 *     eureka:
 *       server-url: ${EUREKA_SERVER_URL:http://localhost:8761/eureka}
 *       instance:
 *         prefer-ip-address: true
 *         lease-renewal-interval-seconds: 10
 *         lease-expiration-duration-seconds: 30
 * }</pre>
 *
 * <h2>Docker/Kubernetes</h2>
 * <pre>{@code
 * docker run -e EUREKA_SERVER_URL=http://eureka:8761/eureka myapp
 * }</pre>
 */
package be.aidji.boot.discovery;