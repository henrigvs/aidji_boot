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

package be.aidji.boot.web.aspect;

import be.aidji.boot.core.exception.ErrorCode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Logs Feign client calls with standardized error handling.
 * <p>
 * Wraps exceptions in {@link be.aidji.boot.core.exception.TechnicalException}
 * using {@link be.aidji.boot.core.exception.CommonErrorCode#EXTERNAL_SERVICE_ERROR}.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @LogFeignClient(clientName = "payment-service")
 * public PaymentResponse processPayment(PaymentRequest request) {
 *     return paymentClient.process(request);
 * }
 * }</pre>
 *
 * <p>If no client name is specified, the declaring class name is used.</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogFeignClient {

    /**
     * Name of the Feign client for logging purposes.
     * If not specified, defaults to the declaring class simple name.
     */
    String clientName() default "";

    /**
     * Whether to rethrow the exception after logging.
     * Default is {@code true}.
     */
    boolean rethrowException() default true;
}