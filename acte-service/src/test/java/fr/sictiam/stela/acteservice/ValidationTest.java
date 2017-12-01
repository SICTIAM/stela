package fr.sictiam.stela.acteservice;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.validation.ObjectError;
import org.springframework.web.multipart.MultipartFile;

import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.ActeNature;
import fr.sictiam.stela.acteservice.model.Admin;
import fr.sictiam.stela.acteservice.validation.ValidationUtil;

public class ValidationTest {

	@Test
	public void testEmailValidation() {
		Admin admin = new Admin("test", "fail@gmail.com", null);
		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
		Set<ConstraintViolation<Admin>> validation = validator.validate(admin);
		assertThat(validation, IsEmptyCollection.empty());

	}

	@Test
	public void testEmailCollectionValidation() {
		List<String> array = new ArrayList<String>();
		array.add("fail");

		Admin admin = new Admin("test", "rapderivas@gmail.com", array);
		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
		Set<ConstraintViolation<Admin>> validation = validator.validate(admin);
		assertThat(validation, not(IsEmptyCollection.empty()));
		assertThat(validation, hasSize(1));
	}

	@Test
	public void testOk() {
		List<String> array = new ArrayList<String>();
		array.add("dev@sictiam.fr");
		Admin admin = new Admin("test", "rapderivas@gmail.com", array);
		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
		Set<ConstraintViolation<Admin>> validation = validator.validate(admin);
		assertThat(validation, IsEmptyCollection.empty());
	}

	@Test
	public void testEmptyActeValidation() {
		Acte acte = new Acte();
		List<ObjectError> errors = ValidationUtil.validateActe(acte);
		assertThat(errors, not(IsEmptyCollection.empty()));
		assertThat(errors, hasSize(5));
	}

	@Test
	public void testActeWithEmptyFileValidation() {
		Acte acte = new Acte("003", LocalDate.now(), ActeNature.ARRETES_INDIVIDUELS, "00", "00", true, true);

		MultipartFile file = new MockMultipartFile("file.csv", "file.csv", "application/test", new byte[0]);
		MultipartFile[] annexes = new MultipartFile[] {};
		List<ObjectError> errors = ValidationUtil.validateActeWithFile(acte, file, annexes);
		assertThat(errors, not(IsEmptyCollection.empty()));
		assertThat(errors, hasSize(1));
		assertThat(errors.get(0).getDefaultMessage(), is("form.validation.mandatoryfile"));
	}

	@Test
	public void testActeFileBadExtensionValidation() {
		Acte acte = new Acte("003", LocalDate.now(), ActeNature.ARRETES_INDIVIDUELS, "00", "00", true, true);

		MultipartFile file = new MockMultipartFile("file.csv", "file.csv", "application/test", new byte[256]);
		MultipartFile[] annexes = new MultipartFile[] {};
		List<ObjectError> errors = ValidationUtil.validateActeWithFile(acte, file, annexes);
		assertThat(errors, not(IsEmptyCollection.empty()));
		assertThat(errors, hasSize(1));
		assertThat(errors.get(0).getDefaultMessage(), is("form.validation.badextension"));
	}

	@Test
	public void testAnnexeBadExtension() {
		Acte acte = new Acte("003", LocalDate.now(), ActeNature.ARRETES_INDIVIDUELS, "00", "00", true, true);

		MultipartFile file = new MockMultipartFile("file.pdf", "file.pdf", "application/test", new byte[256]);
		MultipartFile[] annexes = new MultipartFile[] { new MockMultipartFile("file.csv", new byte[256]) };
		List<ObjectError> errors = ValidationUtil.validateActeWithFile(acte, file, annexes);
		assertThat(errors, not(IsEmptyCollection.empty()));
		assertThat(errors, hasSize(1));
		assertThat(errors.get(0).getDefaultMessage(), is("form.validation.badextension"));

	}

	@Test
	public void testActeOk() {
		Acte acte = new Acte("003", LocalDate.now(), ActeNature.ARRETES_INDIVIDUELS, "00", "00", true, true);

		MultipartFile file = new MockMultipartFile("file.pdf", "file.pdf", "application/test", new byte[256]);

		MultipartFile[] annexes = new MultipartFile[] {};
		List<ObjectError> errors = ValidationUtil.validateActeWithFile(acte, file, annexes);
		assertThat(errors, IsEmptyCollection.empty());

	}

}