package be.aidji.boot.web.aspect;

import be.aidji.boot.core.exception.ErrorCode;
import module java.base;

/**
 * Logs Feign client calls with standardized error handling.
 * <p>
 * Wraps exceptions in {@link be.aidji.boot.core.exception.TechnicalException}
 * with the specified error code.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @LogFeignClient(
 *     clientName = "payment-service",
 *     errorCodeClass = PaymentErrorCode.class,
 *     errorCodeValue = "PAYMENT_SERVICE_ERROR"
 * )
 * public PaymentResponse processPayment(PaymentRequest request) {
 *     return paymentClient.process(request);
 * }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogFeignClient {

    /**
     * Name of the Feign client for logging purposes.
     */
    String clientName() default "";

    /**
     * The ErrorCode enum class.
     */
    Class<? extends ErrorCode> errorCodeClass();

    /**
     * The enum value name within the errorCodeClass.
     */
    String errorCodeValue();

    /**
     * Whether to rethrow the exception after logging.
     */
    boolean rethrowException() default true;
}