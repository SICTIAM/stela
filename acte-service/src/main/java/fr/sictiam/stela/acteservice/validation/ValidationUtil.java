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
            String[] extensions = ActeNature.DOCUMENTS_BUDGETAIRES_ET_FINANCIERS.equals(acte.getNature())
                    ? new String[] { "xml" }
                    : new String[] { "pdf", "jpg", "png" };

            if (!FilenameUtils.isExtension(file.getOriginalFilename(), extensions)) {
                ObjectError objectError = new ObjectError("acte.attachment", "form.validation.badextension");
                errorCopy.add(objectError);
            }
        }
        int i = 0;
        for (MultipartFile annexe : annexes) {
            String[] extensions = ActeNature.DOCUMENTS_BUDGETAIRES_ET_FINANCIERS.equals(acte.getNature())
                    ? new String[] { "pdf", "jpg", "png" }
                    : new String[] { "pdf", "jpg", "png", "xml" };
            if (!FilenameUtils.isExtension(annexe.getOriginalFilename(), extensions)) {
                FieldError objectError = new FieldError("acte", "acte.extensions." + i, "form.validation.badextension");
                errorCopy.add(objectError);
            }
            ++i;
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
