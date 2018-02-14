package fr.sictiam.stela.pesservice.dao;

import fr.sictiam.stela.pesservice.model.SesileConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SesileConfigurationRepository extends JpaRepository<SesileConfiguration, String> {
}