package fr.sictiam.stela.admin.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.admin.model.LocalAuthority;
import fr.sictiam.stela.admin.model.ProvisioningRequest;
import fr.sictiam.stela.admin.model.StatusChangeRequest;
import fr.sictiam.stela.admin.service.LocalAuthorityService;
import fr.sictiam.stela.admin.service.OzwilloProvisioningService;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/ozwillo")
public class OzwilloController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OzwilloController.class);

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    @Value("${ozwillo.application.instanciation_secret}")
    private String instanciationSecret;

    private final OzwilloProvisioningService ozwilloProvisioningService;
    private final LocalAuthorityService localAuthorityService;

    public OzwilloController(OzwilloProvisioningService ozwilloProvisioningService,
            LocalAuthorityService localAuthorityService) {
        this.ozwilloProvisioningService = ozwilloProvisioningService;
        this.localAuthorityService = localAuthorityService;
    }

    @PostMapping("/instance")
    @ResponseBody
    public ResponseEntity<String> create(@RequestBody String requestBody,
            @RequestHeader(name = "X-Hub-Signature") String xHubSignature) {
        LOGGER.debug("Got a provisioning request : {}", requestBody);

        ResponseEntity<String> responseEntity = checkSignature(requestBody, xHubSignature, instanciationSecret);
        if (responseEntity != null)
            return responseEntity;

        ObjectMapper objectMapper = new ObjectMapper();
        ProvisioningRequest provisioningRequest;
        try {
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            provisioningRequest = objectMapper.readValue(requestBody, ProvisioningRequest.class);
            ozwilloProvisioningService.createNewInstance(provisioningRequest);
        } catch (IOException e) {
            LOGGER.error("Unable to parse provisioning request", e);
            return new ResponseEntity<>("Unable to parse provisioning request", HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/status")
    @ResponseBody
    public ResponseEntity<String> status(@RequestBody String requestBody,
            @RequestHeader(name = "X-Hub-Signature") String xHubSignature) {
        LOGGER.debug("Got a status change request : {}", requestBody);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // we have to first read the body to get the instance id
            // to then get the status changed secret required to check the signature of the
            // call
            StatusChangeRequest statusChangeRequest = objectMapper.readValue(requestBody, StatusChangeRequest.class);
            Optional<LocalAuthority> optionalLocalAuthority = localAuthorityService
                    .getByInstanceId(statusChangeRequest.getInstanceId());
            if (!optionalLocalAuthority.isPresent())
                return new ResponseEntity<>(
                        "No local authority found with instance id " + statusChangeRequest.getInstanceId(),
                        HttpStatus.BAD_REQUEST);
            ResponseEntity<String> responseEntity = checkSignature(requestBody, xHubSignature,
                    optionalLocalAuthority.get().getOzwilloInstanceInfo().getStatusChangedSecret());
            if (responseEntity != null)
                return responseEntity;
            ozwilloProvisioningService.changeInstanceStatus(statusChangeRequest);
        } catch (IOException e) {
            LOGGER.error("Unable to parse status change request", e);
            return new ResponseEntity<>("Unable to parse status change request", HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Nullable
    private ResponseEntity<String> checkSignature(String requestBody, String xHubSignature, String secret) {

        if (xHubSignature == null) {
            return new ResponseEntity<>("No X-Hub-Signature header found in request", HttpStatus.FORBIDDEN);
        } else if (!xHubSignature.startsWith("sha1=")) {
            return new ResponseEntity<>("sha1 algo expected in signature", HttpStatus.BAD_REQUEST);
        }

        String receivedHmac = xHubSignature.split("=")[1];
        LOGGER.debug("Received hmac : {}", receivedHmac);
        SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), HMAC_SHA1_ALGORITHM);
        String computedHmac;
        try {
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);
            computedHmac = Hex.encodeHexString(mac.doFinal(requestBody.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Err, sha1 is not available ?!");
            return new ResponseEntity<>("sha1 algo is not available", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (InvalidKeyException e) {
            LOGGER.error("Err, how can my key be invalid ?!");
            return new ResponseEntity<>("Invalid key", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        LOGGER.debug("Computed HMAC : {}", computedHmac);
        if (!receivedHmac.equals(computedHmac)) {
            // throw new BadCredentialsException("Provided HMAC does not conform to what was
            // expected : " + receivedHmac);
            LOGGER.error("Provided HMAC does not conform to what was expected : " + receivedHmac);
            return new ResponseEntity<>("Provided HMAC does not conform to what was expected : \" + receivedHmac",
                    HttpStatus.FORBIDDEN);
        }

        return null;
    }
}
