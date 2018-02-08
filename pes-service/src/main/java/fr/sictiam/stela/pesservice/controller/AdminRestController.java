package fr.sictiam.stela.pesservice.controller;

import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.pesservice.model.Admin;
import fr.sictiam.stela.pesservice.model.ui.Views;
import fr.sictiam.stela.pesservice.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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

    @GetMapping("/alert-message")
    @JsonView(Views.AdminAlertMessageView.class)
    public ResponseEntity<Admin> getAlertMessage() {
        return new ResponseEntity<>(adminService.getAdmin(), HttpStatus.OK);
    }
}
