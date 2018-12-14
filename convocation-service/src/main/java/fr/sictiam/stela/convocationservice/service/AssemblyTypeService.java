package fr.sictiam.stela.convocationservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import fr.sictiam.stela.convocationservice.dao.AssemblyTypeRepository;
import fr.sictiam.stela.convocationservice.model.AssemblyType;
import fr.sictiam.stela.convocationservice.model.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssemblyTypeService {

    private static Logger LOGGER = LoggerFactory.getLogger(AssemblyTypeService.class);

    @Autowired
    AssemblyTypeRepository assemblyTypeRepository;

    @Autowired
    ExternalRestService externalRestService;

    public List<AssemblyType.Light> findAllSimple(String localAuthorityUuid) {

        return assemblyTypeRepository.findAllByLocalAuthorityUuidOrderByNameAsc(localAuthorityUuid);
    }

    public AssemblyType getAssembly(String uuid) throws NotFoundException {

        AssemblyType assemblyType = assemblyTypeRepository.findByUuid(uuid).orElseThrow(NotFoundException::new);
        assemblyType.getProfileUuids().stream().forEach(profile -> {
            JsonNode node = externalRestService.getProfile(profile);
            LOGGER.info("Profile " + node.toString());
            assemblyType.addProfile(profile, node.get("agent"));
        });
        return assemblyType;
    }
}
