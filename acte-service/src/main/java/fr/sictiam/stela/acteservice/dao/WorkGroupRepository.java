package fr.sictiam.stela.acteservice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.sictiam.stela.acteservice.model.Admin;
import fr.sictiam.stela.acteservice.model.WorkGroup;

public interface WorkGroupRepository extends JpaRepository<WorkGroup, String> {

}
