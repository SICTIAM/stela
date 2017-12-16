package fr.sictiam.stela.admin.service;

import fr.sictiam.stela.admin.service.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.sictiam.stela.admin.dao.WorkGroupRepository;
import fr.sictiam.stela.admin.model.LocalAuthority;
import fr.sictiam.stela.admin.model.WorkGroup;

import java.util.List;

@Service
public class WorkGroupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkGroupService.class);

    private final WorkGroupRepository workGroupRepository;
    
    private final LocalAuthorityService localAuthorityService;

    public WorkGroupService(WorkGroupRepository workGroupRepository, LocalAuthorityService localAuthorityService) {
        this.workGroupRepository = workGroupRepository;
        this.localAuthorityService= localAuthorityService;
    }

    public WorkGroup create(WorkGroup workGroup) {
        workGroup= workGroupRepository.save(workGroup);
        LocalAuthority localAuthority= workGroup.getLocalAuthority();
        localAuthority.getGroups().add(workGroup);
        localAuthorityService.createOrUpdate(localAuthority);
        return workGroup;
    }

    public List<WorkGroup> getAllByLocalAuthority(String localAuthorityUuid) {
        return workGroupRepository.findAllByLocalAuthority_Uuid(localAuthorityUuid);
    }

    public WorkGroup getByUuid(String uuid) {
        return workGroupRepository.findByUuid(uuid).orElseThrow(() -> new NotFoundException("notifications.admin.group_not_found"));
    }

    public void deleteGroup(String uuid) {
        WorkGroup workGroup=getByUuid(uuid);
        LocalAuthority localAuthority= workGroup.getLocalAuthority();
        localAuthority.getGroups().remove(workGroup);
        workGroupRepository.delete(workGroup);
        localAuthorityService.createOrUpdate(localAuthority);
    }
}
