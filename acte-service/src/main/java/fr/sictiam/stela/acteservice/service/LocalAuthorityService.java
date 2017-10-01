package fr.sictiam.stela.acteservice.service;

import fr.sictiam.stela.acteservice.dao.LocalAuthorityRepository;
import fr.sictiam.stela.acteservice.model.LocalAuthority;
import fr.sictiam.stela.acteservice.model.xml.RetourClassification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

@Service
public class LocalAuthorityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalAuthorityService.class);

    private final LocalAuthorityRepository localAuthorityRepository;

    private Map<String, Map<String, String>> codesMatieresByLocalAuthority = new HashMap<>();

    @Autowired
    public LocalAuthorityService(LocalAuthorityRepository localAuthorityRepository) {
        this.localAuthorityRepository = localAuthorityRepository;
    }

    public LocalAuthority createOrUpdate(LocalAuthority localAuthority) {
        return localAuthorityRepository.save(localAuthority);
    }

    public void delete(LocalAuthority localAuthority) {
        localAuthorityRepository.delete(localAuthority);
    }

    public List<LocalAuthority> getAll() {
        List<LocalAuthority> localAuthorities = localAuthorityRepository.findAll();
        localAuthorities.sort(Comparator.comparing(LocalAuthority::getName, String.CASE_INSENSITIVE_ORDER));
        return localAuthorities;
    }

    public LocalAuthority getByUuid(String uuid) {
        return localAuthorityRepository.findByUuid(uuid);
    }


    public Optional<LocalAuthority> getByName(String name) {
        return localAuthorityRepository.findByName(name);
    }

    // This is a first raw version, just caching the data in memory
    public Map<String, String> getCodesMatieres(String uuid) {
        if (codesMatieresByLocalAuthority.get(uuid) != null)
            return codesMatieresByLocalAuthority.get(uuid);

        LocalAuthority localAuthority = localAuthorityRepository.findByUuid(uuid);
        Map<String, String> codesMatieres = new LinkedHashMap<>();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(RetourClassification.class);
            InputStream is = new ByteArrayInputStream(localAuthority.getNomenclatureFile());
            RetourClassification classification = (RetourClassification) jaxbContext.createUnmarshaller().unmarshal(is);
            // fist brute version as we currently only deal with the first two levels
            // maybe later make a smooth recursive function :)
            classification.getMatieres().getMatiere1().forEach(matiere1 -> {
                String baseKey = matiere1.getCodeMatiere().toString() + "-";
                matiere1.getMatiere2().forEach(matiere2 -> {
                    String key = baseKey + matiere2.getCodeMatiere().toString() + "-0-0-0";
                    codesMatieres.put(key, matiere2.getLibelle());
                });
            });
        } catch (JAXBException e) {
            LOGGER.error("Unable to parse classification data !", e);
        }

        codesMatieresByLocalAuthority.put(uuid, codesMatieres);
        return codesMatieres;
    }

    public String getCodeMatiereLabel(String localAuthorityUuid, String codeMatiereKey) {
        if (codesMatieresByLocalAuthority.get(localAuthorityUuid) == null)
            getCodesMatieres(localAuthorityUuid);

        return codesMatieresByLocalAuthority.get(localAuthorityUuid).get(codeMatiereKey);
    }
}
