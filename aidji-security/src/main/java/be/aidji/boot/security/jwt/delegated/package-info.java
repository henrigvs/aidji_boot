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
 * Delegated mode JWT implementation.
 *
 * <p>This package provides JWT token validation by delegating to an external OIDC-compliant
 * Identity Provider (Keycloak, Auth0, Okta, Azure AD, etc.) via its JWKS endpoint.
 * No token generation is performed — authentication is fully managed by the external IdP.</p>
 *
 * <h2>Components</h2>
 * <ul>
 *   <li>{@link be.aidji.boot.security.jwt.delegated.JwtTokenVerificatorDelegated} -
 *       Validates tokens using the JWKS from the configured IdP URL</li>
 * </ul>
 *
 * <h2>Configuration</h2>
 * <pre>{@code
 * aidji:
 *   security:
 *     jwt:
 *       mode: delegated
 *       cookie-based: true
 *       cookie-name: jwt-security-principal
 *       delegated:
 *         jwks-url: https://keycloak.example.com/realms/myrealm/protocol/openid-connect/certs
 *         jwks-cache-ttl-seconds: 3600
 * }</pre>
 */
package be.aidji.boot.security.jwt.delegated;
