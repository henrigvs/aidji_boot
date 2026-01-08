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
 * Standalone mode JWT implementation.
 *
 * <p>This package provides a self-contained JWT token generation and validation
 * using RSA key pairs, without requiring external services.</p>
 *
 * <h2>Components</h2>
 * <ul>
 *   <li>{@link be.aidji.boot.security.jwt.stand_alone.JwtTokenProviderStandAlone} - Generates and validates tokens locally</li>
 * </ul>
 *
 * <h2>Configuration with provided keys</h2>
 * <pre>{@code
 * aidji:
 *   security:
 *     jwt:
 *       mode: standalone
 *       standalone:
 *         issuer: my-app
 *         private-key: ${JWT_PRIVATE_KEY}
 *         public-key: ${JWT_PUBLIC_KEY}
 * }</pre>
 *
 * <h2>Configuration with auto-generated keys (development only)</h2>
 * <pre>{@code
 * aidji:
 *   security:
 *     jwt:
 *       mode: standalone
 *       standalone:
 *         issuer: my-app
 *         key-size: 2048
 * }</pre>
 *
 * <p><strong>WARNING:</strong> Auto-generated keys change on restart, invalidating all tokens.
 * Always provide keys in production via environment variables.</p>
 */
package be.aidji.boot.security.jwt.stand_alone;
