package fr.sictiam.stela.admin.controller;

import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.admin.model.GenericAccount;
import fr.sictiam.stela.admin.model.UI.GenericAccountUI;
import fr.sictiam.stela.admin.model.UI.Views;
import fr.sictiam.stela.admin.service.GenericAccountService;
import fr.sictiam.stela.admin.service.LocalAuthorityService;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/generic_account")
public class GenericAccountController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericAccountController.class);

    private final GenericAccountService genericAccountService;
    private final LocalAuthorityService localAuthorityService;
    private final PasswordEncoder passwordEncoder;

    public GenericAccountController(GenericAccountService genericAccountService, PasswordEncoder passwordEncoder,
            LocalAuthorityService localAuthorityService) {
        this.genericAccountService = genericAccountService;
        this.passwordEncoder = passwordEncoder;
        this.localAuthorityService = localAuthorityService;
    }

    @GetMapping("/{uuid}")
    @JsonView(Views.GenericAccountView.class)
    public ResponseEntity<GenericAccount> findByUuid(@PathVariable String uuid) {
        return new ResponseEntity<>(genericAccountService.getByUuid(uuid), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody GenericAccountUI body) {
        GenericAccount genericAccount = new GenericAccount();
        try {
            BeanUtils.copyProperties(genericAccount, body);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.error("Error while updating properties: {}", e);
            return new ResponseEntity<Object>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Optional<GenericAccount> genericAccountSearch = genericAccountService.getByEmail(genericAccount.getEmail());

        if (genericAccountSearch.isPresent()) {
            return new ResponseEntity<Object>("notifications.admin.existing_account.email_conflict",
                    HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.isNotBlank(genericAccount.getSerial()) && StringUtils.isNoneBlank(genericAccount.getVendor())) {
            Optional<GenericAccount> genericAccountSearchCertificate = genericAccountService
                    .getBySerialAndVendor(genericAccount.getSerial(), genericAccount.getVendor());
            if (genericAccountSearchCertificate.isPresent()) {
                return new ResponseEntity<Object>("notifications.admin.existing_account.certificate_conflict",
                        HttpStatus.BAD_REQUEST);
            }
        }

        genericAccount.setPassword(passwordEncoder.encode(genericAccount.getPassword()));
        genericAccount.setLocalAuthorities(body.getLocalAuthorities().stream()
                .map(uuid -> localAuthorityService.getByUuid(uuid)).collect(Collectors.toSet()));
        genericAccountService.save(genericAccount);
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @PostMapping("/authWithCertificate")
    @JsonView(Views.GenericAccountView.class)
    public ResponseEntity<GenericAccount> authWithCertificate(@RequestBody Map<String, String> body) {

        Optional<GenericAccount> genericAccount = genericAccountService.getBySerialAndVendor(body.get("serial"),
                body.get("vendor"));

        if (genericAccount.isPresent()) {
            return new ResponseEntity<>(genericAccount.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

    }

    @PostMapping("/authWithEmailPassword")
    @JsonView(Views.GenericAccountView.class)
    public ResponseEntity<Object> authWithEmailPassword(@RequestBody Map<String, String> body) {

        Optional<GenericAccount> genericAccount = genericAccountService.getByEmail(body.get("email"));
        if (genericAccount.isPresent()
                && passwordEncoder.matches(body.get("password"), genericAccount.get().getPassword())) {
            return new ResponseEntity<>(genericAccount.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

}
