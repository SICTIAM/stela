package fr.sictiam.stela.pesservice.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collection;

public class SirenCollectionValidator implements ConstraintValidator<SirenCollection, Collection<String>> {

    @Override
    public void initialize(SirenCollection constraintAnnotation) {

    }

    @Override
    public boolean isValid(Collection<String> values, ConstraintValidatorContext context) {
        if (values == null) {
            return true;
        }
        for (String s : values) {
            if (!s.matches("[\\d]{9}")) {
                return false;
            }
        }
        return true;
    }
}
