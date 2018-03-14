package fr.sictiam.stela.admin.controller;

import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.admin.model.GenericAccount;
import fr.sictiam.stela.admin.model.UI.Views;
import fr.sictiam.stela.admin.service.GenericAccountService;
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

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/generic_account")
public class GenericAccountController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericAccountController.class);

    private final GenericAccountService genericAccountService;
    private final PasswordEncoder passwordEncoder;

    public GenericAccountController(GenericAccountService genericAccountService, PasswordEncoder passwordEncoder) {
        this.genericAccountService = genericAccountService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/{uuid}")
    @JsonView(Views.GenericAccountView.class)
    public ResponseEntity<GenericAccount> findByUuid(@PathVariable String uuid) {
        return new ResponseEntity<>(genericAccountService.getByUuid(uuid), HttpStatus.OK);
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
