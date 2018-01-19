package fr.sictiam.stela.pesservice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.sictiam.stela.pesservice.model.Admin;

public interface AdminRepository extends JpaRepository<Admin, String> {

}
