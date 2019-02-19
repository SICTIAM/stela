package fr.sictiam.stela.convocationservice.dao;

import fr.sictiam.stela.convocationservice.model.AssemblyType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssemblyTypeRepository extends JpaRepository<AssemblyType, String> {

    public Optional<AssemblyType> findByUuidAndLocalAuthorityUuid(String uuid, String localAuthorityUuid);


    public List<AssemblyType> findAllByLocalAuthorityUuidAndActiveTrue(String localAuthorityUuid, Sort order);
}
