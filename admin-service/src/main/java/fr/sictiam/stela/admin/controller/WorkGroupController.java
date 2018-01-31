package fr.sictiam.stela.admin.controller;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.sictiam.stela.admin.model.WorkGroup;
import fr.sictiam.stela.admin.model.UI.WorkGroupUI;
import fr.sictiam.stela.admin.service.WorkGroupService;

@RestController
@RequestMapping("/api/admin/group")
public class WorkGroupController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkGroupController.class);

    private final WorkGroupService workGroupService;

    public WorkGroupController(WorkGroupService workGroupService) {
        this.workGroupService = workGroupService;
    }


    @PatchMapping("/{uuid}")
    public ResponseEntity<?> updateProfile(@PathVariable String uuid, @RequestBody WorkGroupUI workGroupUI) {
        WorkGroup workGroup = workGroupService.getByUuid(uuid);
        try {
            BeanUtils.copyProperties(workGroup, workGroupUI);
        } catch (Exception e) {
            LOGGER.error("Error while updating properties: {}", e);
            return new ResponseEntity<Object>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        workGroupService.update(workGroup);
        return new ResponseEntity<Object>(HttpStatus.OK);
    }
}
