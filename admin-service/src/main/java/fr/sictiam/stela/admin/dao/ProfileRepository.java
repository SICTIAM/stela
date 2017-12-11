package fr.sictiam.stela.admin.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.sictiam.stela.admin.model.Profile;

public interface ProfileRepository extends JpaRepository<Profile, String> {

}
