package fr.sictiam.stela.pesservice.validation;

import javax.validation.Constraint;
import javax.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SirenCollectionValidator.class)
@Documented
public @interface SirenCollection {
    String message() default "Invalid Siren";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}