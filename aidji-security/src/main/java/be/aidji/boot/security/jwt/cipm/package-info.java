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
 * CIPM mode JWT implementations.
 *
 * <p>This package provides JWT token generation and validation using an external CIPM
 * (Centralized Identity and Policy Management) service backed by HashiCorp Vault.</p>
 *
 * <h2>Components</h2>
 * <ul>
 *   <li>{@link be.aidji.boot.security.jwt.cipm.JwtTokenProviderCipm} - Generates tokens via CIPM API</li>
 *   <li>{@link be.aidji.boot.security.jwt.cipm.JwtTokenVerificatorCipm} - Validates tokens using JWKS from CIPM</li>
 * </ul>
 *
 * <h2>Configuration</h2>
 * <pre>{@code
 * aidji:
 *   security:
 *     jwt:
 *       mode: cipm
 *       generation-enabled: true
 *       cipm-properties:
 *         base-url: https://cipm.example.com
 *         public-key-uri: /.well-known/jwks.json
 *         sign-token-uri: /api/sign-token
 *         api-token: ${CIPM_API_TOKEN}
 *         issuer: cipm-issuer
 *         jwks-cache-ttl-seconds: 3600
 * }</pre>
 */
package be.aidji.boot.security.jwt.cipm;
