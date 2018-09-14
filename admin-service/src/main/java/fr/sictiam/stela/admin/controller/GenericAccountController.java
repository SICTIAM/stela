package fr.sictiam.stela.admin.controller;

import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.admin.model.GenericAccount;
import fr.sictiam.stela.admin.model.PaullConnection;
import fr.sictiam.stela.admin.model.UI.GenericAccountUI;
import fr.sictiam.stela.admin.model.UI.SearchResultsUI;
import fr.sictiam.stela.admin.model.UI.Views;
import fr.sictiam.stela.admin.service.GenericAccountService;
import fr.sictiam.stela.admin.service.LocalAuthorityService;
import fr.sictiam.stela.admin.service.PaullConnectionService;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/generic_account")
public class GenericAccountController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericAccountController.class);

    private final GenericAccountService genericAccountService;
    private final LocalAuthorityService localAuthorityService;
    private final PaullConnectionService paullConnectionService;
    private final PasswordEncoder passwordEncoder;

    public GenericAccountController(GenericAccountService genericAccountService, PasswordEncoder passwordEncoder,
            LocalAuthorityService localAuthorityService, PaullConnectionService paullConnectionService) {
        this.genericAccountService = genericAccountService;
        this.passwordEncoder = passwordEncoder;
        this.localAuthorityService = localAuthorityService;
        this.paullConnectionService = paullConnectionService;
    }

    @GetMapping("/{uuid}")
    @JsonView(Views.GenericAccountView.class)
    public ResponseEntity<GenericAccount> findByUuid(@PathVariable String uuid) {
        return new ResponseEntity<>(genericAccountService.getByUuid(uuid), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<SearchResultsUI> getAllGenericAccount(
            @RequestParam(value = "search", required = false, defaultValue = "") String search,
            @RequestParam(value = "software", required = false, defaultValue = "") String software,
            @RequestParam(value = "email", required = false, defaultValue = "") String email,
            @RequestParam(value = "limit", required = false, defaultValue = "25") Integer limit,
            @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
            @RequestParam(value = "column", required = false, defaultValue = "email") String column,
            @RequestParam(value = "direction", required = false, defaultValue = "ASC") String direction,
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin) {
        if (!isLocalAuthorityAdmin) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<GenericAccount> genericAccounts = genericAccountService.getAllWithPagination(search, software, email,
                limit, offset, column, direction);
        Long count = genericAccountService.countAll(search, software, email);
        return new ResponseEntity<>(new SearchResultsUI(count, genericAccounts), HttpStatus.OK);
    }

    @GetMapping("/session/{sessionID}")
    public ResponseEntity<PaullConnection> getSession(@PathVariable String sessionID) {
        return new ResponseEntity<>(paullConnectionService.getBySessionID(sessionID), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody GenericAccountUI body,
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin) {
        if (!isLocalAuthorityAdmin) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
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
                    HttpStatus.CONFLICT);
        }

        if (StringUtils.isNotBlank(genericAccount.getSerial()) && StringUtils.isNoneBlank(genericAccount.getVendor())) {
            Optional<GenericAccount> genericAccountSearchCertificate = genericAccountService
                    .getBySerialAndVendor(genericAccount.getSerial(), genericAccount.getVendor());
            if (genericAccountSearchCertificate.isPresent()) {
                return new ResponseEntity<Object>("notifications.admin.existing_account.certificate_conflict",
                        HttpStatus.CONFLICT);
            }
        }

        genericAccount.setPassword(passwordEncoder.encode(genericAccount.getPassword()));
        genericAccount.setLocalAuthorities(body.getLocalAuthoritySirens().stream()
                .map(uuid -> localAuthorityService.getByUuid(uuid)).collect(Collectors.toSet()));
        genericAccountService.save(genericAccount);
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @PutMapping("/{uuid}")
    public ResponseEntity<?> update(@PathVariable String uuid, @RequestBody GenericAccountUI body,
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin) {
        if (!isLocalAuthorityAdmin) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        GenericAccount genericAccount = genericAccountService.getByUuid(uuid);
        String currentPassword = genericAccount.getPassword();
        try {
            BeanUtils.copyProperties(genericAccount, body);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.error("Error while updating properties: {}", e);
            return new ResponseEntity<Object>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Optional<GenericAccount> genericAccountSearch = genericAccountService.getByEmail(genericAccount.getEmail());

        if (genericAccountSearch.isPresent()
                && !genericAccountSearch.get().getUuid().equals(genericAccount.getUuid())) {
            return new ResponseEntity<Object>("notifications.admin.existing_account.email_conflict",
                    HttpStatus.CONFLICT);
        }

        if (StringUtils.isNotBlank(genericAccount.getSerial()) && StringUtils.isNoneBlank(genericAccount.getVendor())) {
            Optional<GenericAccount> genericAccountSearchCertificate = genericAccountService
                    .getBySerialAndVendor(genericAccount.getSerial(), genericAccount.getVendor());
            if (genericAccountSearchCertificate.isPresent()
                    && !genericAccountSearch.get().getUuid().equals(genericAccount.getUuid())) {
                return new ResponseEntity<Object>("notifications.admin.existing_account.certificate_conflict",
                        HttpStatus.CONFLICT);
            }
        }

        genericAccount.setPassword(StringUtils.isNotBlank(genericAccount.getPassword()) ?
                passwordEncoder.encode(genericAccount.getPassword()) : currentPassword);
        genericAccount.getLocalAuthorities().clear();
        genericAccount.getLocalAuthorities().addAll(body.getLocalAuthoritySirens().stream()
                .map(localAuthorityUuid -> localAuthorityService.getByUuid(localAuthorityUuid))
                .collect(Collectors.toSet()));
        genericAccountService.save(genericAccount);
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity delete(@PathVariable String uuid,
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin) {
        if (!isLocalAuthorityAdmin) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        
        genericAccountService.deleteByUuid(uuid);
        return new ResponseEntity(HttpStatus.OK);
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
