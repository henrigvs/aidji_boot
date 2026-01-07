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

import be.aidji.boot.core.exception.AidjiException;
import be.aidji.boot.core.exception.CommonErrorCode;
import be.aidji.boot.core.exception.TechnicalException;
import feign.FeignException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

/**
 * Aspect that provides logging and error handling for Feign client calls.
 * <p>
 * This aspect intercepts methods annotated with {@link LogFeignClient} and provides:
 * <ul>
 *   <li>Entry and exit logging with execution time</li>
 *   <li>Standardized exception handling and wrapping</li>
 *   <li>Automatic conversion of {@link FeignException} to {@link TechnicalException}</li>
 * </ul>
 *
 * <h2>Exception Handling Strategy</h2>
 * <table border="1">
 *   <tr><th>Exception Type</th><th>Behavior</th></tr>
 *   <tr><td>{@link FeignException}</td><td>Wrapped in {@link TechnicalException} with configured error code</td></tr>
 *   <tr><td>{@link AidjiException}</td><td>Re-thrown as-is (already properly formatted)</td></tr>
 *   <tr><td>Other {@link Throwable}</td><td>Wrapped in {@link TechnicalException} with INTERNAL_ERROR</td></tr>
 * </table>
 *
 * <h2>Log Output Format</h2>
 * <pre>
 * [payment-service] &lt;-- processPayment
 * [payment-service] --&gt; processPayment (145ms)
 * </pre>
 *
 * @see LogFeignClient
 * @see TechnicalException
 */
@Aspect
class LogFeignClientAspect {

    /**
     * Intercepts methods annotated with {@link LogFeignClient} and provides
     * logging and standardized error handling for Feign client calls.
     *
     * @param joinPoint the join point representing the intercepted method
     * @return the result of the method execution, or {@code null} if an exception
     *         was caught and {@link LogFeignClient#rethrowException()} is {@code false}
     * @throws TechnicalException if a {@link FeignException} or unexpected error occurs
     *                            and rethrowing is enabled
     * @throws AidjiException if an {@link AidjiException} is caught and rethrowing is enabled
     */
    @Around("@annotation(be.aidji.boot.web.aspect.LogFeignClient)")
    Object logFeignCall(ProceedingJoinPoint joinPoint) {
        Logger log = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        LogFeignClient logFeignClient = signature.getMethod().getAnnotation(LogFeignClient.class);
        String clientName = resolveClientName(logFeignClient, signature);
        String methodName = signature.getName();

        log.info("[{}] <-- {}", clientName, methodName);
        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            log.info("[{}] --> {} ({}ms)", clientName, methodName, duration);
            return result;

        } catch (FeignException e) {
            handleFeignException(e, clientName, logFeignClient.rethrowException());

        } catch (AidjiException e) {
            handleAidjiException(e, methodName, log, logFeignClient.rethrowException());

        } catch (Throwable e) {
            handleUnexpectedException(e, methodName, log, logFeignClient.rethrowException());
        }

        return null;
    }

    /**
     * Resolves the client name from annotation or falls back to declaring class name.
     */
    private String resolveClientName(LogFeignClient annotation, MethodSignature signature) {
        return annotation.clientName().isEmpty()
                ? signature.getDeclaringType().getSimpleName()
                : annotation.clientName();
    }

    /**
     * Handles Feign-specific exceptions by wrapping them in a TechnicalException
     * with {@link CommonErrorCode#EXTERNAL_SERVICE_ERROR}.
     */
    private void handleFeignException(FeignException e, String clientName, boolean rethrow) {
        String error = e.contentUTF8();
        HttpStatus status = HttpStatus.valueOf(e.status());
        String errorMessage = String.format(
                "Call to %s failed with status %s -> error: %s",
                clientName, status, error
        );

        if (rethrow) {
            throw new TechnicalException(CommonErrorCode.EXTERNAL_SERVICE_ERROR, errorMessage, e);
        }
    }

    /**
     * Handles Aidji framework exceptions by logging and optionally rethrowing.
     */
    private void handleAidjiException(AidjiException e, String methodName,
                                      Logger log, boolean rethrow) {
        log.error("{}: {} -> {}", methodName, e.getErrorCode(), e.getMessage(), e);

        if (rethrow) {
            throw e;
        }
    }

    /**
     * Handles unexpected exceptions by wrapping them in a TechnicalException.
     */
    private void handleUnexpectedException(Throwable e, String methodName,
                                           Logger log, boolean rethrow) {
        log.error("{}: Unexpected error -> {}", methodName, e.getMessage(), e);

        if (rethrow) {
            throw new TechnicalException(CommonErrorCode.INTERNAL_ERROR, e.getMessage(), e.getCause());
        }
    }
}