package fr.sictiam.stela.admin.service;

import fr.sictiam.stela.admin.dao.WorkGroupRepository;
import fr.sictiam.stela.admin.model.LocalAuthority;
import fr.sictiam.stela.admin.model.UI.WorkGroupUI;
import fr.sictiam.stela.admin.model.WorkGroup;
import fr.sictiam.stela.admin.service.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WorkGroupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkGroupService.class);

    private final WorkGroupRepository workGroupRepository;

    private final LocalAuthorityService localAuthorityService;

    public WorkGroupService(WorkGroupRepository workGroupRepository, LocalAuthorityService localAuthorityService) {
        this.workGroupRepository = workGroupRepository;
        this.localAuthorityService = localAuthorityService;
    }

    public WorkGroup create(WorkGroup workGroup) {
        LocalAuthority localAuthority = workGroup.getLocalAuthority();
        workGroup = workGroupRepository.save(workGroup);
        localAuthority.getGroups().add(workGroup);
        localAuthorityService.createOrUpdate(localAuthority);
        return workGroup;
    }

    public WorkGroup createFromUI(WorkGroupUI workGroupUI, String localAuthorityUuid) {
        LocalAuthority localAuthority = localAuthorityService.getByUuid(localAuthorityUuid);
        WorkGroup workGroup = new WorkGroup(localAuthority, workGroupUI.getName());
        workGroup.setRights(workGroupUI.getRights());
        return create(workGroup);
    }

    public WorkGroup createFromModules(String name, Set<String> rights, String localAuthorityUuid) {
        LocalAuthority localAuthority = localAuthorityService.getByUuid(localAuthorityUuid);
        WorkGroup workGroup = new WorkGroup(localAuthority, name);
        workGroup.setRights(rights);
        return create(workGroup);
    }

    public List<WorkGroup> getAllByLocalAuthority(String localAuthorityUuid) {
        return workGroupRepository.findAllByLocalAuthority_Uuid(localAuthorityUuid);
    }

    public List<WorkGroup> getAllByLocalAuthorityAndModule(String localAuthorityUuid, String moduleName) {
        List<WorkGroup> allGroups = getAllByLocalAuthority(localAuthorityUuid);
        return allGroups.stream()
                .filter(workGroup -> workGroup.getRights().stream()
                        .anyMatch(right -> right.startsWith(moduleName + "_")))
                .collect(Collectors.toList());
    }

    public WorkGroup getByUuid(String uuid) {
        return workGroupRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException("notifications.admin.group_not_found"));
    }

    public void deleteGroup(String uuid) {
        WorkGroup workGroup = getByUuid(uuid);
        LocalAuthority localAuthority = workGroup.getLocalAuthority();
        localAuthority.getGroups().remove(workGroup);
        workGroupRepository.delete(workGroup);
        localAuthorityService.createOrUpdate(localAuthority);
    }

    public WorkGroup update(WorkGroup workGroup) {
        return workGroupRepository.save(workGroup);
    }
}
