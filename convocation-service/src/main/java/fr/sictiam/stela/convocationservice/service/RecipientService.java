package fr.sictiam.stela.convocationservice.service;

import fr.sictiam.stela.convocationservice.dao.RecipientRepository;
import fr.sictiam.stela.convocationservice.model.LocalAuthority;
import fr.sictiam.stela.convocationservice.model.Recipient;
import fr.sictiam.stela.convocationservice.model.exception.NotFoundException;
import fr.sictiam.stela.convocationservice.model.exception.RecipientExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;

@Service
public class RecipientService {

    private static Logger LOGGER = LoggerFactory.getLogger(RecipientService.class);

    @Autowired
    RecipientRepository recipientRepository;

    @Autowired
    ExternalRestService externalRestService;

    @Autowired
    LocalAuthorityService localAuthorityService;


    public Recipient createFrom(String firstname, String lastname, String email,
            String phoneNumber, String localAuthorityUuid) {

        LocalAuthority localAuthority = localAuthorityService.getByUuid(localAuthorityUuid);

        Optional<Recipient> exist = recipientRepository.findByEmailAndLocalAuthorityUuid(email, localAuthorityUuid);
        if (exist.isPresent()) {
            LOGGER.error("A recipient with email {} already exists in local authority {}", email, localAuthority.getName());
            throw new RecipientExistsException();
        }

        Recipient recipient = new Recipient(firstname, lastname, email, phoneNumber, localAuthority);
        recipient.setToken(generateToken(recipient));
        recipient.setAssemblyTypes(new HashSet<>());
        return recipient;
    }

    public Recipient getRecipient(String uuid) {

        return recipientRepository.findByUuid(uuid).orElseThrow(NotFoundException::new);
    }

    public Recipient save(Recipient recipient) {

        return recipientRepository.saveAndFlush(recipient);
    }

    public void setActive(String uuid, boolean active) {
        Recipient recipient = getRecipient(uuid);
        recipient.setActive(active);
        save(recipient);
    }

    public String generateToken(Recipient recipient) {
        try {
            StringBuilder sb = new StringBuilder(recipient.getEmail());
            sb.append(Instant.now().getEpochSecond());
            sb.append(new Random().nextLong());
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(sb.toString().getBytes("UTF-8"));

            StringBuilder token = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                token.append(String.format("%02x",
                        b & 0xff));
            }
            return token.toString();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.warn("No SHA-256 algorithm found");
        } catch (UnsupportedEncodingException e) {
            LOGGER.warn("Unsupported UTF-8 encoding");
        }
        return "default";
    }
}
