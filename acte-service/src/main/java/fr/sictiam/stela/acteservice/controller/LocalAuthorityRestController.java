package fr.sictiam.stela.acteservice.controller;

import fr.sictiam.stela.acteservice.model.LocalAuthority;
import fr.sictiam.stela.acteservice.service.LocalAuthorityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/acte/localAuthority")
public class LocalAuthorityRestController {

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
    public ResponseEntity<LocalAuthority> updateProperty(@PathVariable String uuid, @RequestBody LocalAuthority localAuthority) {
        localAuthority = localAuthorityService.partialUpdate(localAuthority, uuid);
        return new ResponseEntity<>(localAuthority, HttpStatus.OK);
    }

}
