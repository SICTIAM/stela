package fr.sictiam.stela.acteservice.dao;

import fr.sictiam.stela.acteservice.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, String> {

}
