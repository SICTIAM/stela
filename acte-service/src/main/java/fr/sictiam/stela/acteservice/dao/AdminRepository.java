package fr.sictiam.stela.acteservice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.sictiam.stela.acteservice.model.Admin;

public interface AdminRepository extends JpaRepository<Admin, String> {

}
