package fr.sictiam.stela.convocationservice.controller;

import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.convocationservice.model.Right;
import fr.sictiam.stela.convocationservice.model.Tag;
import fr.sictiam.stela.convocationservice.model.ui.Views;
import fr.sictiam.stela.convocationservice.model.util.RightUtils;
import fr.sictiam.stela.convocationservice.service.TagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/convocation/tag")
public class TagRestController {


    private final static Logger LOGGER = LoggerFactory.getLogger(TagRestController.class);

    private final TagService tagService;

    @Autowired
    public TagRestController(
            TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/all")
    @JsonView(Views.Tag.class)
    public ResponseEntity<List<Tag>> getTags(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {

        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.CONVOCATION_ADMIN))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(tagService.getTags(currentLocalAuthUuid), HttpStatus.OK);
    }

    @PostMapping
    @JsonView(Views.Tag.class)
    public ResponseEntity<Tag> create(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestBody Tag params) {

        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.CONVOCATION_ADMIN))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Tag tag = tagService.create(params, currentLocalAuthUuid);

        return new ResponseEntity<>(tag, HttpStatus.OK);
    }

    @GetMapping("/{uuid}")
    @JsonView(Views.Tag.class)
    public ResponseEntity<Tag> get(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @PathVariable String uuid) {

        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.CONVOCATION_ADMIN))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Tag tag = tagService.getTag(uuid, currentLocalAuthUuid);

        return new ResponseEntity<>(tag, HttpStatus.OK);
    }

    @PutMapping("/{uuid}")
    @JsonView(Views.Tag.class)
    public ResponseEntity<Tag> update(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @PathVariable String uuid,
            @RequestBody Tag params) {

        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.CONVOCATION_ADMIN))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Tag tag = tagService.update(uuid, currentLocalAuthUuid, params);

        return new ResponseEntity<>(tag, HttpStatus.OK);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> delete(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @PathVariable String uuid) {

        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.CONVOCATION_ADMIN))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            tagService.delete(uuid, currentLocalAuthUuid);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (DataIntegrityViolationException e) {
            LOGGER.error("Tag {} is still affected to an attachment", uuid);
            return new ResponseEntity<>("convocation.errors.tag.used", HttpStatus.CONFLICT);
        }
    }
}
