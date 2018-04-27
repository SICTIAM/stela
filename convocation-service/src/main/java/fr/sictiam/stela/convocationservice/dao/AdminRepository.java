package fr.sictiam.stela.convocationservice.dao;

import fr.sictiam.stela.convocationservice.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, String> {

}
