package fr.sictiam.stela.acteservice.service;

import fr.sictiam.stela.acteservice.dao.LocalAuthorityRepository;
import fr.sictiam.stela.acteservice.dao.MaterialCodeRepository;
import fr.sictiam.stela.acteservice.model.LocalAuthority;
import fr.sictiam.stela.acteservice.model.MaterialCode;
import fr.sictiam.stela.acteservice.model.xml.DemandeClassification;
import fr.sictiam.stela.acteservice.model.xml.ObjectFactory;
import fr.sictiam.stela.acteservice.model.xml.RetourClassification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;

@Service
public class LocalAuthorityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalAuthorityService.class);

    private final LocalAuthorityRepository localAuthorityRepository;

    private final MaterialCodeRepository materialCodeRepository;

    @Autowired
    public LocalAuthorityService(LocalAuthorityRepository localAuthorityRepository,
            MaterialCodeRepository materialCodeRepository) {
        this.localAuthorityRepository = localAuthorityRepository;
        this.materialCodeRepository = materialCodeRepository;
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

    public Optional<LocalAuthority> getBySiren(String siren) {
        return localAuthorityRepository.findBySiren(siren);
    }
    
    @Transactional
    public void loadCodesMatieres(String uuid) {

        try {
            LocalAuthority localAuthority = localAuthorityRepository.findByUuid(uuid);
            JAXBContext jaxbContext = JAXBContext.newInstance(RetourClassification.class);
            InputStream is = new ByteArrayInputStream(localAuthority.getNomenclatureFile());
            RetourClassification classification = (RetourClassification) jaxbContext.createUnmarshaller().unmarshal(is);
            loadCodesMatieres(uuid, classification);

        } catch (JAXBException e) {
            LOGGER.error("Unable to parse classification data !", e);
        }
    }
    
    @Transactional
    public void loadCodesMatieres(String uuid, RetourClassification classification) {

        List<MaterialCode> codes = new ArrayList<>();
        LocalAuthority localAuthority = localAuthorityRepository.findByUuid(uuid);
        materialCodeRepository.deleteAll(localAuthority.getMaterialCodes());
        classification.getMatieres().getMatiere1().forEach(matiere1 -> {
            String key1 = matiere1.getCodeMatiere().toString();
            String label1 = matiere1.getLibelle();
            if (!matiere1.getMatiere2().isEmpty()) {
                matiere1.getMatiere2().forEach(matiere2 -> {
                    String key2 = key1 + "-" + matiere2.getCodeMatiere().toString();
                    String label2 = label1 + " / " + matiere2.getLibelle();

                    if (!matiere2.getMatiere3().isEmpty()) {
                        matiere2.getMatiere3().forEach(matiere3 -> {
                            String key3 = key2 + "-" + matiere3.getCodeMatiere().toString();
                            String label3 = label2 + " / " + matiere3.getLibelle();
                            if (!matiere3.getMatiere4().isEmpty()) {
                                matiere3.getMatiere4().forEach(matiere4 -> {
                                    String key4 = key3 + "-" + matiere4.getCodeMatiere().toString();
                                    String label4 = label3 + " / " + matiere4.getLibelle();
                                    if (!matiere4.getMatiere5().isEmpty()) {
                                        matiere4.getMatiere5().forEach(matiere5 -> {
                                            String key5 = key4 + "-" + matiere5.getCodeMatiere().toString();
                                            String label5 = label4 + " / " + matiere5.getLibelle();
                                            codes.add(createMaterialCode(key5, label5, localAuthority));
                                        });
                                    } else {
                                        codes.add(createMaterialCode(key4 + "-0", label4, localAuthority));
                                    }
                                });
                            } else {
                                codes.add(createMaterialCode(key3 + "-0-0", label3, localAuthority));
                            }
                        });

                    } else {
                        codes.add(createMaterialCode(key2 + "-0-0-0", label2, localAuthority));
                    }

                });
            } else {
                codes.add(createMaterialCode(key1 + "-0-0-0-0", label1, localAuthority));
            }

        });

        localAuthority.setNomenclatureDate(classification.getDateClassification());
        localAuthority.setMaterialCodes(codes);
        localAuthorityRepository.save(localAuthority);

    }
    
    private MaterialCode createMaterialCode(String key, String label, LocalAuthority localAuthority) { 
        MaterialCode newMat = new MaterialCode(key, label, localAuthority); 
        materialCodeRepository.save(newMat); 
        return newMat; 
    } 


    @Transactional
    public Map<String, String> getCodesMatieres(String uuid) {

        LocalAuthority localAuthority = localAuthorityRepository.findByUuid(uuid);
        Map<String, String> codesMatieres = new LinkedHashMap<>();
        if (localAuthority.getMaterialCodes() == null || localAuthority.getMaterialCodes().isEmpty()) {
            loadCodesMatieres(uuid);
        }
        localAuthority.getMaterialCodes()
                .forEach(materialCode -> codesMatieres.put(materialCode.getCode(), materialCode.getLabel()));
        return codesMatieres;
    }

    public String getCodeMatiereLabel(String localAuthorityUuid, String codeMatiereKey) {
        Optional<MaterialCode> materialCode = materialCodeRepository.findByCodeAndLocalAuthorityUuid(codeMatiereKey,
                localAuthorityUuid);
        if (materialCode.isPresent())
            return materialCode.get().getLabel();
        
        return null;
    }

}
