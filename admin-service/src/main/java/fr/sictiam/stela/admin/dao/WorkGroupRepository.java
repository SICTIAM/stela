package fr.sictiam.stela.admin.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.sictiam.stela.admin.model.Profile;
import fr.sictiam.stela.admin.model.WorkGroup;

public interface WorkGroupRepository extends JpaRepository<WorkGroup, String> {

}
