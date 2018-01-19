package fr.sictiam.stela.pesservice.service;

import org.springframework.stereotype.Service;

import fr.sictiam.stela.pesservice.dao.AdminRepository;
import fr.sictiam.stela.pesservice.model.Admin;

@Service
public class AdminService {

	private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
		this.adminRepository = adminRepository;
	}

	public Admin create(Admin adminModule) {
		return adminRepository.save(adminModule);
	}

	public void update(Admin adminModule) {
		adminRepository.save(adminModule);
	}

	public Admin getAdmin() {
		return adminRepository.findAll().get(0);
	}
}
