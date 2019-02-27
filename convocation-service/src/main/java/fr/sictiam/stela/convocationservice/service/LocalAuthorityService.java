package fr.sictiam.stela.convocationservice.service;

import fr.sictiam.stela.convocationservice.dao.AttachmentRepository;
import fr.sictiam.stela.convocationservice.dao.LocalAuthorityRepository;
import fr.sictiam.stela.convocationservice.model.Attachment;
import fr.sictiam.stela.convocationservice.model.LocalAuthority;
import fr.sictiam.stela.convocationservice.model.event.FileUploadEvent;
import fr.sictiam.stela.convocationservice.model.event.LocalAuthorityEvent;
import fr.sictiam.stela.convocationservice.model.exception.ConvocationFileException;
import fr.sictiam.stela.convocationservice.model.exception.NotFoundException;
import fr.sictiam.stela.convocationservice.model.util.ConvocationBeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;

import java.io.IOException;
import java.util.Optional;

@Service
public class LocalAuthorityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalAuthorityService.class);


    private final LocalAuthorityRepository localAuthorityRepository;

    private final AttachmentRepository attachmentRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public LocalAuthorityService(
            LocalAuthorityRepository localAuthorityRepository,
            AttachmentRepository attachmentRepository,
            ApplicationEventPublisher applicationEventPublisher) {
        this.localAuthorityRepository = localAuthorityRepository;
        this.attachmentRepository = attachmentRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public LocalAuthority createOrUpdate(LocalAuthority localAuthority) {
        return localAuthorityRepository.save(localAuthority);
    }

    public LocalAuthority getLocalAuthority(String uuid) {
        return localAuthorityRepository.findByUuid(uuid).orElseThrow(NotFoundException::new);
    }

    public LocalAuthority update(String uuid, LocalAuthority params) {

        LocalAuthority localAuthority = getLocalAuthority(uuid);
        ConvocationBeanUtils.mergeProperties(params, localAuthority, "uuid", "name");

        return localAuthorityRepository.saveAndFlush(localAuthority);
    }

    public void delete(LocalAuthority localAuthority) {
        localAuthorityRepository.delete(localAuthority);
    }

    public LocalAuthority getByUuid(String uuid) {
        return localAuthorityRepository.findByUuid(uuid).get();
    }

    public Optional<LocalAuthority> getByName(String name) {
        return localAuthorityRepository.findByName(name);
    }

    public Optional<LocalAuthority> getBySiren(String siren) {
        return localAuthorityRepository.findBySiren(siren);
    }

    public void addProcuration(LocalAuthority localAuthority, MultipartFile procuration) {

        try {
            Attachment attachment = new Attachment(procuration.getOriginalFilename(), procuration.getBytes());
            attachment = attachmentRepository.save(attachment);
            localAuthority.setDefaultProcuration(attachment);
            localAuthorityRepository.save(localAuthority);
            applicationEventPublisher.publishEvent(new FileUploadEvent(this, attachment));
        } catch (IOException e) {
            throw new ConvocationFileException("Failed to get bytes from file " + procuration.getOriginalFilename(), e);
        }
    }

    @Transactional
    public void handleEvent(LocalAuthorityEvent event) throws IOException {
        LocalAuthority localAuthority = localAuthorityRepository.findByUuid(event.getUuid())
                .orElse(new LocalAuthority(event.getUuid(), event.getName(), event.getSlugName(), event.getSiren()));

        // Update existing local authorities with new slug name
        localAuthority.setSlugName(event.getSlugName());

        createOrUpdate(localAuthority);
    }

}
