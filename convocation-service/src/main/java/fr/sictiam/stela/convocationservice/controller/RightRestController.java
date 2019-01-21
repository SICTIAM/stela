package fr.sictiam.stela.convocationservice.controller;

import fr.sictiam.stela.convocationservice.model.Right;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/convocation/rights")
public class RightRestController {

    @GetMapping
    public List<Right> getAllRights() {
        return Arrays.asList(Right.values());
    }
}
