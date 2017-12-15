package fr.sictiam.stela.admin.service;

import fr.sictiam.stela.admin.service.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fr.sictiam.stela.admin.dao.WorkGroupRepository;
import fr.sictiam.stela.admin.model.WorkGroup;

import java.util.List;

@Service
public class WorkGroupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkGroupService.class);

    private final WorkGroupRepository workGroupRepository;

    public WorkGroupService(WorkGroupRepository workGroupRepository) {
        this.workGroupRepository = workGroupRepository;
    }

    public WorkGroup create(WorkGroup workGroup) {
        return workGroupRepository.save(workGroup);
    }

    public List<WorkGroup> getAllByLocalAuthority(String localAuthorityUuid) {
        return workGroupRepository.findAllByLocalAuthority_Uuid(localAuthorityUuid);
    }

    public WorkGroup getByUuid(String uuid) {
        return workGroupRepository.findByUuid(uuid).orElseThrow(() -> new NotFoundException("notifications.admin.group_not_found"));
    }

    public void deleteGroup(String uuid) {
        workGroupRepository.delete(getByUuid(uuid));
    }
}
