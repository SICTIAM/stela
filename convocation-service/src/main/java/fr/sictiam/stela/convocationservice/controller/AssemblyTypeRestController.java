package fr.sictiam.stela.convocationservice.controller;

import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.convocationservice.model.AssemblyType;
import fr.sictiam.stela.convocationservice.model.LocalAuthority;
import fr.sictiam.stela.convocationservice.model.Recipient;
import fr.sictiam.stela.convocationservice.model.Right;
import fr.sictiam.stela.convocationservice.model.ui.SearchResultsUI;
import fr.sictiam.stela.convocationservice.model.ui.Views;
import fr.sictiam.stela.convocationservice.model.util.RightUtils;
import fr.sictiam.stela.convocationservice.service.AssemblyTypeService;
import fr.sictiam.stela.convocationservice.service.LocalAuthorityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/convocation/assembly-type")
public class AssemblyTypeRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssemblyTypeRestController.class);

    @Autowired
    private AssemblyTypeService assemblyTypeService;

    @Autowired
    private LocalAuthorityService localAuthorityService;

    @JsonView(Views.Public.class)
    @GetMapping("/all")
    public ResponseEntity<List<AssemblyType>> getAllByLocalAuthority(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {

        if (!RightUtils.hasRight(rights, Arrays.asList(Right.values()))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<AssemblyType> assemblyTypes = assemblyTypeService.findAllSorted(currentLocalAuthUuid);
        return new ResponseEntity<>(assemblyTypes, HttpStatus.OK);
    }

    @JsonView(Views.SearchAssemblyType.class)
    @GetMapping
    public ResponseEntity<SearchResultsUI> getAll(
            @RequestParam(value = "multifield", required = false) String multifield,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "limit", required = false, defaultValue = "25") Integer limit,
            @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
            @RequestParam(value = "column", required = false, defaultValue = "name") String column,
            @RequestParam(value = "direction", required = false, defaultValue = "ASC") String direction,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {

        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.CONVOCATION_ADMIN))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<AssemblyType> recipients = assemblyTypeService.findAllWithQuery(multifield, name, location, active,
                limit, offset, column, direction, currentLocalAuthUuid);

        Long count = assemblyTypeService.countAllWithQuery(multifield, name, location, active, currentLocalAuthUuid);

        return new ResponseEntity<>(new SearchResultsUI(count, recipients), HttpStatus.OK);
    }

    @JsonView(Views.AssemblyTypeInternal.class)
    @GetMapping("/{uuid}")
    public ResponseEntity<AssemblyType> getAssemblyType(
            @PathVariable String uuid,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {

        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.CONVOCATION_ADMIN))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        AssemblyType assemblyType = assemblyTypeService.getAssemblyType(uuid, currentLocalAuthUuid);
        return new ResponseEntity<>(assemblyType, HttpStatus.OK);
    }


    @JsonView(Views.AssemblyTypeInternal.class)
    @PostMapping
    public ResponseEntity<?> create(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Profile-UUID") String currentProfileUuid,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestBody AssemblyType assemblyType) {

        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.CONVOCATION_ADMIN))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        assemblyType = assemblyTypeService.create(assemblyType, currentLocalAuthUuid, currentProfileUuid);
        return new ResponseEntity<>(assemblyType, HttpStatus.OK);
    }

    @JsonView(Views.AssemblyTypeInternal.class)
    @PutMapping("/{uuid}")
    public ResponseEntity<?> update(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Profile-UUID") String currentProfileUuid,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @PathVariable String uuid,
            @RequestBody AssemblyType assemblyType) {

        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.CONVOCATION_ADMIN))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        assemblyType = assemblyTypeService.update(uuid, currentLocalAuthUuid, assemblyType);
        return new ResponseEntity<>(assemblyType, HttpStatus.OK);
    }

    @JsonView(Views.AssemblyType.class)
    @GetMapping("/delay")
    public ResponseEntity<AssemblyType> getDelay(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {

        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.CONVOCATION_ADMIN))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        LocalAuthority localAuthority = localAuthorityService.getLocalAuthority(currentLocalAuthUuid);
        AssemblyType assemblyType = new AssemblyType();
        assemblyType.setDelay(localAuthority.getResidentThreshold() ? 5 : 3);

        return new ResponseEntity<>(assemblyType, HttpStatus.OK);
    }

    @JsonView(Views.Public.class)
    @GetMapping("/{uuid}/recipients")
    public ResponseEntity<List<Recipient>> getRecipients(
            @PathVariable String uuid,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {

        if (!RightUtils.hasRight(rights, Arrays.asList(Right.CONVOCATION_DEPOSIT, Right.CONVOCATION_ADMIN))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<Recipient> recipients = assemblyTypeService.findRecipients(uuid, currentLocalAuthUuid);
        return new ResponseEntity<>(recipients, HttpStatus.OK);
    }
}