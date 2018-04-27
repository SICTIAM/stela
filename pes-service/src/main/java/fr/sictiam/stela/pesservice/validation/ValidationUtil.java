package fr.sictiam.stela.pesservice.validation;


import fr.sictiam.stela.pesservice.model.PesAller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

import javax.validation.Validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationUtil {

    public static List<ObjectError> validatePes(PesAller pesAller) {
        Validator validator = new SpringValidatorAdapter(Validation.buildDefaultValidatorFactory().getValidator());

        Errors errors = new BeanPropertyBindingResult(pesAller, "pesAller");
        ValidationUtils.invokeValidator(validator, pesAller, errors, PesAller.RestValidation.class);
        List<ObjectError> errorCopy = new ArrayList<>();
        for (ObjectError error : errors.getAllErrors()) {
            errorCopy.add(error);
        }
        return errorCopy;
    }
}
