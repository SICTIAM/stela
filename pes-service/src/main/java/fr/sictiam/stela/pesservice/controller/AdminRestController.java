package fr.sictiam.stela.pesservice.controller;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.sictiam.stela.pesservice.model.Admin;
import fr.sictiam.stela.pesservice.service.AdminService;

@RestController
@RequestMapping("/api/pes/admin")
public class AdminRestController {

    private final AdminService adminService;

    public AdminRestController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PatchMapping
    public void update(@Valid @RequestBody Admin adminModule) {
        adminService.update(adminModule);
    }

    @GetMapping
    public ResponseEntity<Admin> getModuleParams() {
        return new ResponseEntity<>(adminService.getAdmin(), HttpStatus.OK);
    }

}
