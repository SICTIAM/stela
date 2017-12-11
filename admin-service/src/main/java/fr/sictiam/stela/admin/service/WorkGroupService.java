package fr.sictiam.stela.admin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fr.sictiam.stela.admin.dao.WorkGroupRepository;
import fr.sictiam.stela.admin.model.WorkGroup;

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
}
