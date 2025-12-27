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
 * HTTP filters for request/response processing.
 *
 * <p>This package provides servlet filters for cross-cutting concerns:</p>
 *
 * <ul>
 *   <li>{@link be.aidji.boot.web.filter.RequestLoggingFilter} - Logs HTTP requests and responses
 *       with configurable verbosity (method, URI, status, duration, headers, payload)</li>
 * </ul>
 *
 * <h2>Request Logging</h2>
 * <p>The logging filter captures:</p>
 * <ul>
 *   <li>HTTP method and URI</li>
 *   <li>Request/response headers (optional)</li>
 *   <li>Request/response payload (optional, with size limit)</li>
 *   <li>Response status code</li>
 *   <li>Request duration in milliseconds</li>
 *   <li>Client IP address</li>
 * </ul>
 *
 * <h2>Configuration</h2>
 * <pre>{@code
 * aidji:
 *   web:
 *     request-logging:
 *       enabled: true
 *       include-headers: true
 *       include-payload: false
 *       max-payload-length: 1000
 * }</pre>
 *
 * <h2>Example Log Output</h2>
 * <pre>{@code
 * INFO  RequestLoggingFilter - [REQUEST] POST /api/users from 127.0.0.1
 * INFO  RequestLoggingFilter - [RESPONSE] POST /api/users -> 201 Created (45ms)
 * }</pre>
 *
 * <p>With headers and payload enabled:</p>
 * <pre>{@code
 * INFO  RequestLoggingFilter - [REQUEST] POST /api/users from 127.0.0.1
 *   Headers: {Content-Type=[application/json], Accept=[application/json]}
 *   Payload: {"name":"John Doe","email":"john@example.com"}
 * INFO  RequestLoggingFilter - [RESPONSE] POST /api/users -> 201 Created (45ms)
 *   Headers: {Content-Type=[application/json]}
 *   Payload: {"id":123,"name":"John Doe","email":"john@example.com"}
 * }</pre>
 *
 * @see be.aidji.boot.web.filter.RequestLoggingFilter
 * @see be.aidji.boot.web.AidjiWebProperties
 */
package be.aidji.boot.web.filter;