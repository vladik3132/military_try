package ua.edu.viti.military.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {FutureDateValidator.class}) // <-- має бути масив
@Documented
public @interface FutureDate {

    String message() default "Дата має бути в майбутньому";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
