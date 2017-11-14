package fr.sictiam.stela.acteservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.sictiam.stela.acteservice.model.Admin;
import fr.sictiam.stela.acteservice.service.AdminService;

@RequestMapping("/api/acte/admin")
public class AdminRestController {

	private final AdminService adminService;

	public AdminRestController(AdminService adminService) {
		this.adminService = adminService;
	}

	public void updateMailInfo(@RequestBody Admin adminModule) {
		adminService.updateMailInfo(adminModule);
	}

	@GetMapping
	public ResponseEntity<Admin> getModuleParams() {
		return new ResponseEntity<>(adminService.getAdmin(), HttpStatus.OK);
	}

}
