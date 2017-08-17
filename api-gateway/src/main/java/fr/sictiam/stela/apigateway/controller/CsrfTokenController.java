package fr.sictiam.stela.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/csrf-token")
public class CsrfTokenController {

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<String> fakeCallToGetCsrfToken() {
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
