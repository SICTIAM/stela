package fr.sictiam.stela.acteservice.service.init;

import fr.sictiam.stela.acteservice.dao.ActeHistoryRepository;
import fr.sictiam.stela.acteservice.dao.ActeRepository;
import fr.sictiam.stela.acteservice.model.*;
import fr.sictiam.stela.acteservice.service.ActeService;
import fr.sictiam.stela.acteservice.service.LocalAuthorityService;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Profile("bootstrap-data")
public class DataInitializerService implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializerService.class);

    private final ActeService acteService;
    private final ActeRepository acteRepository;
    private final LocalAuthorityService localAuthorityService;

    @Autowired
    public DataInitializerService(ActeService acteService, ActeRepository acteRepository, LocalAuthorityService localAuthorityService) {
        this.acteService = acteService;
        this.acteRepository = acteRepository;
        this.localAuthorityService = localAuthorityService;
    }

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        LOGGER.info("Bootstrap data asked");
        importData();
    }

    private void importData() {

        // --- Local Authorities ---

        LocalAuthority localAuthority001 = new LocalAuthority("SICTIAM-Test", "999888777", "999", "1", "31");
        localAuthorityService.createOrUpdate(localAuthority001);

        LocalAuthority localAuthority002 = new LocalAuthority("Vallauris", "666555444", "666", "1", "31");
        localAuthorityService.createOrUpdate(localAuthority002);

        LocalAuthority localAuthority003 = new LocalAuthority("Valbonne", "333222111", "333", "1", "31");
        localAuthorityService.createOrUpdate(localAuthority003);

        LOGGER.info("Bootstrapped some local authorities");


        // --- Actes ---

        Acte acte001 = new Acte("001", LocalDate.now(), ActeNature.DELIBERATIONS, "1-0-0-1-0", "STELA 3 sera fini en Décembre", true, true);
        createDummyActe(acte001);

        Acte acte002 = new Acte("002", LocalDate.now(), ActeNature.DELIBERATIONS, "1-0-0-1-0", "SESILE 4 sera fini quand il sera fini", true, true);
        createDummyActe(acte002);

        Acte acte003 = new Acte("003", LocalDate.now(), ActeNature.DELIBERATIONS, "1-0-0-1-0", "Le DC Exporter sera mis en attente", true, true);
        acte003 = createDummyActe(acte003);

        try {
            // sleep some seconds to let async creation of the archive happens
            Thread.sleep(2000);
        } catch (Exception e) {
            LOGGER.error("Should not have thrown an exception");
        }
        addARStatus(acte003.getUuid());

        LOGGER.info("Bootstrapped some Actes");
    }

    private Acte createDummyActe(Acte acte) {
        try {
            MultipartFile actePDF = getMultipartResourceFile("examples/acte.pdf", "application/pdf");
            MultipartFile annexe1 = getMultipartResourceFile("examples/annexe1.xml", "text/xml");
            MultipartFile annexe2 = getMultipartResourceFile("examples/annexe2.xml", "text/xml");

            LocalAuthority currentLocalAuthority = localAuthorityService.getAll().get(0);

            return acteService.create(currentLocalAuthority, acte, actePDF, annexe1, annexe2);
        } catch (IOException e) {
            LOGGER.error("Unable to bootstrap acte {} : {}", acte.getNumber(), e.toString());
            return acte;
        }
    }

    private void addARStatus(String uuid) {
        ActeHistory acteHistory = new ActeHistory(uuid, StatusType.ACK_RECEIVED, LocalDateTime.now(), null, null);
        Acte acte = acteService.getByUuid(uuid);
        SortedSet<ActeHistory> acteHistories = acte.getActeHistories();
        acteHistories.add(acteHistory);
        acte.setActeHistories(acteHistories);
        acteRepository.save(acte);
    }

    private MultipartFile getMultipartResourceFile(String filename, String contentType) throws IOException {
        File file = new ClassPathResource(filename).getFile();

        FileInputStream input = new FileInputStream(file);
        return new MockMultipartFile(file.getName(), file.getName(), contentType, IOUtils.toByteArray(input));
    }
}