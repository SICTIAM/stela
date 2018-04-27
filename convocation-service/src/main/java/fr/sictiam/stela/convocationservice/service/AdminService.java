package fr.sictiam.stela.convocationservice.service;

import fr.sictiam.stela.convocationservice.dao.AdminRepository;
import fr.sictiam.stela.convocationservice.model.Admin;
import org.springframework.stereotype.Service;

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
