package fr.sictiam.stela.convocationservice.controller;

import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.convocationservice.dao.AssemblyTypeRepository;
import fr.sictiam.stela.convocationservice.model.AssemblyType;
import fr.sictiam.stela.convocationservice.model.Right;
import fr.sictiam.stela.convocationservice.model.ui.SearchResultsUI;
import fr.sictiam.stela.convocationservice.model.ui.Views;
import fr.sictiam.stela.convocationservice.model.util.RightUtils;
import fr.sictiam.stela.convocationservice.service.AssemblyTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/convocation/assembly-type")
public class AssemblyTypeRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssemblyTypeRestController.class);

    @Autowired
    AssemblyTypeRepository assemblyTypeRepository;

    @Autowired
    AssemblyTypeService assemblyTypeService;


    @Autowired
    public AssemblyTypeRestController() {
    }

    @JsonView(Views.Public.class)
    @GetMapping
    public ResponseEntity<SearchResultsUI> getAll(
            @RequestParam(value = "multifield", required = false) String multifield,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "limit", required = false, defaultValue = "25") Integer limit,
            @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
            @RequestParam(value = "column", required = false, defaultValue = "name") String column,
            @RequestParam(value = "direction", required = false, defaultValue = "ASC") String direction,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {

        if (!RightUtils.hasRight(rights, Arrays.asList(Right.values()))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<AssemblyType> recipients = assemblyTypeService.findAllWithQuery(multifield, name, active,
                limit, offset, column, direction, currentLocalAuthUuid);

        Long count = assemblyTypeService.countAllWithQuery(multifield, name, active, currentLocalAuthUuid);

        return new ResponseEntity<>(new SearchResultsUI(count, recipients), HttpStatus.OK);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<AssemblyType> getAssemblyType(
            @PathVariable String uuid,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {

        if (!RightUtils.hasRight(rights, Arrays.asList(Right.values()))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(assemblyTypeService.getAssemblyType(uuid), HttpStatus.OK);
    }


    @PostMapping
    public ResponseEntity<?> create(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Profile-UUID") String currentProfileUuid,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {

        return new ResponseEntity<>(HttpStatus.OK);
    }


    private String getContentType(String filename) {
        String mimeType = URLConnection.guessContentTypeFromName(filename);
        if (mimeType == null) {
            LOGGER.info("Mimetype is not detectable, will take default");
            mimeType = "application/octet-stream";
        }
        return mimeType;
    }
}
