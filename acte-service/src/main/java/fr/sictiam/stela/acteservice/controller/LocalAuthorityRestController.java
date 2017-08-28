package fr.sictiam.stela.acteservice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.acteservice.config.LocalDateTimeConverter;
import fr.sictiam.stela.acteservice.model.LocalAuthority;
import fr.sictiam.stela.acteservice.service.LocalAuthorityService;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/acte/localAuthority")
public class LocalAuthorityRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalAuthorityRestController.class);

    private final LocalAuthorityService localAuthorityService;

    @Autowired
    public LocalAuthorityRestController(LocalAuthorityService localAuthorityService){
        this.localAuthorityService = localAuthorityService;
    }

    @GetMapping
    public ResponseEntity<List<LocalAuthority>> getAll() {
        List<LocalAuthority> localAuthorities = localAuthorityService.getAll();
        return new ResponseEntity<>(localAuthorities, HttpStatus.OK);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<LocalAuthority> getByUuid(@PathVariable String uuid) {
        LocalAuthority localAuthority = localAuthorityService.getByUuid(uuid);
        return new ResponseEntity<>(localAuthority, HttpStatus.OK);
    }

    @PatchMapping("/{uuid}")
    public ResponseEntity<LocalAuthority> updateProperty(@PathVariable String uuid, @RequestBody String requestData) {
        LocalAuthority localAuthority = localAuthorityService.getByUuid(uuid);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ConvertUtils.register(new LocalDateTimeConverter(), LocalDateTime.class);
            Map<String, Object> dataMap =
                    objectMapper.readValue(requestData, new TypeReference<Map<String, Object>>(){});
            for (String key : dataMap.keySet()) {
                BeanUtils.copyProperty(localAuthority, key, dataMap.get(key));
            }
        } catch (Exception e) {
            LOGGER.error("Error while mapping received JSON data", e);
            return new ResponseEntity<>(localAuthority, HttpStatus.BAD_REQUEST);
        }
        LOGGER.debug("Local authority is now {}", localAuthority.toString());
        localAuthority = localAuthorityService.createOrUpdate(localAuthority);
        return new ResponseEntity<>(localAuthority, HttpStatus.OK);
    }

}
