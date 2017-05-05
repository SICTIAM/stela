package fr.sictiam.stela.pescommand.validator;

import fr.sictiam.stela.pescommand.command.CreatePesCommand;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Created by s.vergon on 04/05/2017.
 */
public class PescommandValidator implements Validator {

    public boolean supports(Class clazz) {
        return CreatePesCommand.class.equals(clazz);
    }
    public void validate(Object obj, Errors e) {
        ValidationUtils.rejectIfEmpty(e, "title", "Le Titre ou objet doit être renseigné");
        ValidationUtils.rejectIfEmpty(e, "fileContent", "Le contenu de l'objet doit être renseigné");
    }
}
