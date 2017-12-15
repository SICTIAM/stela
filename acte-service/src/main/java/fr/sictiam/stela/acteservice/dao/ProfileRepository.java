package fr.sictiam.stela.acteservice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.sictiam.stela.acteservice.model.Profile;

public interface ProfileRepository extends JpaRepository<Profile, String> {

}
