package fr.sictiam.stela.acteservice.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.sictiam.stela.acteservice.model.Right;

@RestController
@RequestMapping("/api/acte/rights")
public class RightRestController {

    @GetMapping
    public List<Right> getAllRights() {
        return Arrays.asList(Right.values());
    }
}
