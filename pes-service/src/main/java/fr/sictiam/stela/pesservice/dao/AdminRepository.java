package fr.sictiam.stela.pesservice.dao;

import fr.sictiam.stela.pesservice.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, String> {

}
