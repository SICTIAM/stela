package fr.sictiam.stela.acteservice.service;

import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.ActeNature;
import fr.sictiam.stela.acteservice.model.ActeParams;
import fr.sictiam.stela.acteservice.model.dsc.DocumentBudgetaire;
import nu.xom.ParsingException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.*;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Validation;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ValidationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationService.class);

    @Autowired
    private ExternalRestService externalRestService;

    public List<ObjectError> validateActeParams(ActeParams acte) {

        // Annotations validation
        List<ObjectError> errorCopy = validateActe(acte);

        // Validate acte file
        if (acte.getFile() != null && !acte.getFile().isEmpty()) {
            errorCopy.addAll(validateFile(acte.getFile(), acte.getNature(), true, "file"));
        }

        // Annexes validation
        acte.getAnnexes().forEach(annexe -> {
            if (getTypeForAnnexe(annexe.getOriginalFilename(), acte.getAnnexeTypes()) == null) {
                errorCopy.add(new FieldError("acte", "annexeType", "Missing file type for " + annexe.getOriginalFilename()));
            }
            errorCopy.addAll(validateFile(annexe, acte.getNature(), false, "annexes"));
        });

        // Check if email belongs to localAuthority
        if (StringUtils.isNotEmpty(acte.getEmail())) {
            try {
                if (externalRestService.getProfileForEmail(acte.getLocalAuthority().getSiren(), acte.getEmail()) == null) {
                    errorCopy.add(new FieldError("acte", "email", acte.getEmail() + " does not belong to " + acte.getLocalAuthority().getName()));
                }
            } catch (IOException e) {
                LOGGER.warn("Cannot check if {} belongs to {}", acte.getEmail(), acte.getLocalAuthority().getName());
            }
        }
        return errorCopy;
    }



    public List<ObjectError> validateActeWithFile(Acte acte, MultipartFile file, MultipartFile... annexes) {

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

    public List<ObjectError> validateFile(MultipartFile file, ActeNature acteNature, boolean isActeFile, String field) {
        List<ObjectError> errorCopy = new ArrayList<>();
        String[] extensions = isActeFile ?
                (ActeNature.DOCUMENTS_BUDGETAIRES_ET_FINANCIERS.equals(acteNature)
                        ? new String[]{"xml"}
                        : new String[]{"pdf", "jpg", "png"}) :
                (ActeNature.DOCUMENTS_BUDGETAIRES_ET_FINANCIERS.equals(acteNature)
                        ? new String[]{"pdf", "jpg", "png"}
                        : new String[]{"pdf", "jpg", "png", "xml"});
        if (!FilenameUtils.isExtension(file.getOriginalFilename(), extensions)) {
            ObjectError objectError = new FieldError("acte", field, file.getOriginalFilename(), false, null, null, "form.validation.badextension");
            errorCopy.add(objectError);
        }
        if (ActeNature.DOCUMENTS_BUDGETAIRES_ET_FINANCIERS.equals(acteNature)
                && FilenameUtils.isExtension(file.getOriginalFilename(), new String[]{"xml"})) {
            LOGGER.info("Budget file detected, verifying the seal...");
            try {
                DocumentBudgetaire documentBudgetaire = DocumentBudgetaire.buildFromBytes(file.getBytes());
                if (!documentBudgetaire.isSealed()) {
                    LOGGER.info("The budget file is not sealed, refusing the updload");
                    errorCopy.add(new FieldError("acte", field, file.getOriginalFilename(), false, null, null, "form.validation.budgetfilenotsealed"));
                } else {
                    if (documentBudgetaire.checkSealIfExist()) {
                        LOGGER.info("Budget file is correctly sealed");
                    } else {
                        LOGGER.info("The budget file is not correctly sealed, refusing the updload");
                        errorCopy.add(new FieldError("acte", field, file.getOriginalFilename(), false, null, null, "form.validation.badsealbudgetfile"));
                    }
                }
            } catch (IOException | ParsingException e) {
                LOGGER.error("Error while trying to validate budget file: {}", e.getMessage());
                errorCopy.add(new FieldError("acte", field, file.getOriginalFilename(), false, null, null, "form.validation.badbudgetfile"));
            } catch (ParserConfigurationException e) {
                LOGGER.error("Error while trying to calculate the stamp file : {}", e.getMessage());
                errorCopy.add(new FieldError("acte", field, file.getOriginalFilename(), false, null, null, "form.validation.badstampbudgetfile"));
            } catch (Exception e) {
                LOGGER.error("Budget file malformed: {}", e.getMessage());
                errorCopy.add(new FieldError("acte", field, file.getOriginalFilename(), false, null, null, "form.validation.budgetfilemalformed"));
            }
        }
        return errorCopy;
    }

    public List<ObjectError> validateActe(Acte acte) {
        Validator validator = new SpringValidatorAdapter(Validation.buildDefaultValidatorFactory().getValidator());

        Errors errors = new BeanPropertyBindingResult(acte, "acte");
        ValidationUtils.invokeValidator(validator, acte, errors, Acte.RestValidation.class);
        return new ArrayList<>(errors.getAllErrors());

    }

    public List<ObjectError> validateActe(ActeParams acte) {
        Validator validator = new SpringValidatorAdapter(Validation.buildDefaultValidatorFactory().getValidator());

        Errors errors = new BeanPropertyBindingResult(acte, "acte");
        ValidationUtils.invokeValidator(validator, acte, errors);
        return new ArrayList<>(errors.getAllErrors());
    }

    private String getTypeForAnnexe(String filename, List<String> types) {
        Pattern pattern = Pattern.compile("(.*):(.*)");
        for (String type : types) {
            Matcher m = pattern.matcher(type);
            if (m.find() && m.groupCount() > 1 && filename.equals(m.group(1))) {
                return m.group(2);
            }
        }
        return null;
    }
}
