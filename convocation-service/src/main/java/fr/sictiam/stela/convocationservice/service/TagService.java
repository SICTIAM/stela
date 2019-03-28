package fr.sictiam.stela.convocationservice.service;

import fr.sictiam.stela.convocationservice.dao.TagRepository;
import fr.sictiam.stela.convocationservice.model.LocalAuthority;
import fr.sictiam.stela.convocationservice.model.Tag;
import fr.sictiam.stela.convocationservice.model.exception.NotFoundException;
import fr.sictiam.stela.convocationservice.model.util.ConvocationBeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TagService.class);

    private final TagRepository tagRepository;

    private final LocalAuthorityService localAuthorityService;

    @Autowired
    public TagService(
            TagRepository tagRepository,
            LocalAuthorityService localAuthorityService) {
        this.tagRepository = tagRepository;
        this.localAuthorityService = localAuthorityService;
    }

    public List<Tag> getTags(String localAuthorityUuid) {

        return tagRepository.findTagsByLocalAuthorityUuidOrderByName(localAuthorityUuid);
    }

    public Tag getTag(String uuid, String localAuthorityUuid) {

        return tagRepository
                .findByUuidAndLocalAuthorityUuid(uuid, localAuthorityUuid)
                .orElseThrow(() -> new NotFoundException("Tag " + uuid + " not found in local authority " + localAuthorityUuid));
    }


    public Tag create(Tag params, String localAuthorityUuid) {

        LocalAuthority localAuthority = localAuthorityService.getLocalAuthority(localAuthorityUuid);

        Tag tag = new Tag();
        ConvocationBeanUtils.mergeProperties(params, tag);
        tag.setLocalAuthority(localAuthority);

        return tagRepository.saveAndFlush(tag);
    }

    public Tag update(String uuid, String localAuthorityUuid, Tag params) {

        Tag tag = getTag(uuid, localAuthorityUuid);
        ConvocationBeanUtils.mergeProperties(params, tag, "uuid", "localAuthority");

        return tagRepository.saveAndFlush(tag);
    }

    public void delete(String uuid, String localAuthorityUuid) {

        Tag tag = getTag(uuid, localAuthorityUuid);
        tagRepository.delete(tag);
    }
}
