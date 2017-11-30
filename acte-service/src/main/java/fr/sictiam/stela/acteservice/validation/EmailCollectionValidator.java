package fr.sictiam.stela.acteservice.validation;

import java.util.Collection;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;

public class EmailCollectionValidator implements ConstraintValidator<EmailCollection, Collection<String>> {

    @Override
    public void initialize(EmailCollection constraintAnnotation) {

    }

    @Override
    public boolean isValid(Collection<String> value, ConstraintValidatorContext context) {
	if (value == null) {
	    return true;
	}
	EmailValidator validator = new EmailValidator();
	for (String s : value) {
	    if (!validator.isValid(s, context)) {
		return false;
	    }
	}
	return true;
    }
}
