package fr.sictiam.stela.admin.dao;

import fr.sictiam.stela.admin.model.Instance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstanceRepository extends JpaRepository<Instance, String> {
}
