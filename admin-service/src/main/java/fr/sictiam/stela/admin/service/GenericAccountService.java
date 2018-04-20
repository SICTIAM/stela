package fr.sictiam.stela.admin.service;

import fr.sictiam.stela.admin.dao.GenericAccountRepository;
import fr.sictiam.stela.admin.model.GenericAccount;
import fr.sictiam.stela.admin.service.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GenericAccountService {

    private final GenericAccountRepository genericAccountRepository;

    public GenericAccountService(GenericAccountRepository genericAccountRepository) {
        this.genericAccountRepository = genericAccountRepository;
    }

    public GenericAccount save(GenericAccount genericAccount) {
        return genericAccountRepository.save(genericAccount);
    }

    public GenericAccount getByUuid(String uuid) {
        return genericAccountRepository.findById(uuid).orElseThrow(NotFoundException::new);
    }

    public Optional<GenericAccount> getBySerialAndVendor(String serial, String vendor) {
        return genericAccountRepository.findBySerialAndVendor(serial, vendor);
    }

    public Optional<GenericAccount> getByEmail(String email) {
        return genericAccountRepository.findByEmail(email);
    }

}
