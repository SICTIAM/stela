package fr.sictiam.stela.acteservice.service;

import fr.sictiam.stela.acteservice.dao.AdminRepository;
import fr.sictiam.stela.acteservice.model.Admin;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

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
        List<Admin> admins = adminRepository.findAll();
        if (admins.size() == 0) {
            create(new Admin("", Collections.emptyList(), true, LocalDateTime.now(), LocalDateTime.now(), false, ""));
            return adminRepository.findAll().get(0);
        } else {
            return admins.get(0);
        }
    }

    public boolean isMiatAvailable() {
        Admin admin = getAdmin();
        if (!admin.isMiatAvailable())
            return false;
        LocalDateTime today = LocalDateTime.now();
        return today.isBefore(admin.getUnavailabilityMiatStartDate())
                || today.isAfter(admin.getUnavailabilityMiatEndDate());
    }
}
