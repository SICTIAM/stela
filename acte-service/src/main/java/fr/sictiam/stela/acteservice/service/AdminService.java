package fr.sictiam.stela.acteservice.service;

import org.springframework.stereotype.Service;

import fr.sictiam.stela.acteservice.dao.AdminRepository;
import fr.sictiam.stela.acteservice.model.Admin;

import java.time.LocalDateTime;

@Service
public class AdminService {

	private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
		this.adminRepository = adminRepository;
	}

	public Admin create(Admin adminModule) {
		return adminRepository.save(adminModule);
	}

	public void updateAdmin(Admin adminModule) {
		adminRepository.save(adminModule);
	}

	public Admin getAdmin() {
		return adminRepository.findAll().get(0);
	}

	public boolean isMiatAccessible() {
    	Admin admin = getAdmin();
    	if(!admin.isMiatAccessible()) return false;
		LocalDateTime today = LocalDateTime.now();
		return !(today.isAfter(admin.getInaccessibilityMiatStartDate()) && today.isBefore(admin.getInaccessibilityMiatEndDate()));
	}
}
