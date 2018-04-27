package fr.sictiam.stela.acteservice.validation;

import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.ActeNature;
import org.apache.commons.io.FilenameUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationUtil {
    public static List<ObjectError> validateActeWithFile(Acte acte, MultipartFile file, MultipartFile... annexes) {

        List<ObjectError> errorCopy = validateActe(acte);
        if (file.isEmpty()) {
            FieldError objectError = new FieldError("acte", "acteAttachment", "form.validation.mandatoryfile");
            errorCopy.add(objectError);
        } else {
            errorCopy.addAll(validateFile(file, acte.getNature(), true, "acteAttachment"));
        }
        int i = 0;
        for (MultipartFile annexe : annexes) {
            errorCopy.addAll(validateFile(annexe, acte.getNature(), false, "acte.extensions." + i));
            ++i;
        }
        if (ActeNature.DOCUMENTS_BUDGETAIRES_ET_FINANCIERS.equals(acte.getNature()) && i == 0) {
            FieldError objectError = new FieldError("acte", "annexes", "form.validation.mandatoryfile");
            errorCopy.add(objectError);
        }
        return errorCopy;
    }

    public static List<ObjectError> validateFile(MultipartFile file, ActeNature acteNature, boolean isActeFile, String field) {
        List<ObjectError> errorCopy = new ArrayList<>();
        String[] extensions = isActeFile ?
                (ActeNature.DOCUMENTS_BUDGETAIRES_ET_FINANCIERS.equals(acteNature)
                        ? new String[]{"xml"}
                        : new String[]{"pdf", "jpg", "png"}) :
                (ActeNature.DOCUMENTS_BUDGETAIRES_ET_FINANCIERS.equals(acteNature)
                        ? new String[]{"pdf", "jpg", "png"}
                        : new String[]{"pdf", "jpg", "png", "xml"});
        if (!FilenameUtils.isExtension(file.getOriginalFilename(), extensions)) {
            ObjectError objectError = new FieldError("acte", field, "form.validation.badextension");
            errorCopy.add(objectError);
        }
        return errorCopy;
    }

    public static List<ObjectError> validateActe(Acte acte) {
        Validator validator = new SpringValidatorAdapter(Validation.buildDefaultValidatorFactory().getValidator());

        Errors errors = new BeanPropertyBindingResult(acte, "acte");
        ValidationUtils.invokeValidator(validator, acte, errors, Acte.RestValidation.class);
        List<ObjectError> errorCopy = new ArrayList<>();
        for (ObjectError error : errors.getAllErrors()) {
            errorCopy.add(error);
        }
        return errorCopy;
    }
}
