package fr.sictiam.stela.acteservice.service;

import fr.sictiam.stela.acteservice.dao.AttachmentTypeRepository;
import fr.sictiam.stela.acteservice.dao.LocalAuthorityRepository;
import fr.sictiam.stela.acteservice.dao.MaterialCodeRepository;
import fr.sictiam.stela.acteservice.model.ActeNature;
import fr.sictiam.stela.acteservice.model.AttachmentType;
import fr.sictiam.stela.acteservice.model.AttachmentTypeReferencial;
import fr.sictiam.stela.acteservice.model.LocalAuthority;
import fr.sictiam.stela.acteservice.model.MaterialCode;
import fr.sictiam.stela.acteservice.model.event.LocalAuthorityEvent;
import fr.sictiam.stela.acteservice.model.ui.GenericAccount;
import fr.sictiam.stela.acteservice.model.xml.RetourClassification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LocalAuthorityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalAuthorityService.class);

    private final LocalAuthorityRepository localAuthorityRepository;

    private final MaterialCodeRepository materialCodeRepository;

    private final AttachmentTypeRepository attachmentTypeRepository;

    @Autowired
    public LocalAuthorityService(LocalAuthorityRepository localAuthorityRepository,
            MaterialCodeRepository materialCodeRepository, AttachmentTypeRepository attachmentTypeRepository) {
        this.localAuthorityRepository = localAuthorityRepository;
        this.materialCodeRepository = materialCodeRepository;
        this.attachmentTypeRepository = attachmentTypeRepository;
    }

    public LocalAuthority createOrUpdate(LocalAuthority localAuthority) {
        return localAuthorityRepository.save(localAuthority);
    }

    public void delete(LocalAuthority localAuthority) {
        localAuthorityRepository.delete(localAuthority);
    }

    public List<LocalAuthority> getAll() {
        return localAuthorityRepository.findAllByActiveTrueOrderByName();
    }

    public LocalAuthority getByUuid(String uuid) {
        return localAuthorityRepository.findByUuid(uuid).get();
    }

    @Transactional
    public void generateFormMiatReturn(String localAuthorityUuid, Map<String, Object> map) {

        LocalAuthority localAuthority = getByUuid(localAuthorityUuid);
        map.put("codeMatiere", localAuthority.getMaterialCodes().stream()
                .collect(Collectors.toMap(MaterialCode::getCode, MaterialCode::getLabel)));
        map.put("natureActes", localAuthority.getAttachmentTypeReferencials().stream().map(ref -> ref.getActeNature())
                .collect(Collectors.toMap(ActeNature::getCode, ActeNature::name)));
        map.put("collectivite_id", localAuthority.getUuid());
        map.put("collectivite_nom", localAuthority.getName());
        // TODO get the date format
        map.put("collectivite_dateClassification", localAuthority.getNomenclatureDate());
        map.put("collectivite_siren", localAuthority.getSiren());
        map.put("collectivite_departement", localAuthority.getDepartment());
        map.put("collectivite_arrondissement", localAuthority.getDistrict());
        map.put("collectivite_nature", localAuthority.getNature());
    }

    public Optional<LocalAuthority> getByName(String name) {
        return localAuthorityRepository.findByName(name);
    }

    public Optional<LocalAuthority> getBySiren(String siren) {
        return localAuthorityRepository.findBySiren(siren);
    }

    @Transactional
    public void loadClassification(String uuid) {

        try {
            LocalAuthority localAuthority = localAuthorityRepository.findByUuid(uuid).get();
            JAXBContext jaxbContext = JAXBContext.newInstance(RetourClassification.class);
            InputStream is = new ByteArrayInputStream(localAuthority.getNomenclatureFile());
            RetourClassification classification = (RetourClassification) jaxbContext.createUnmarshaller().unmarshal(is);
            loadClassification(uuid, classification);

        } catch (JAXBException e) {
            LOGGER.error("Unable to parse classification data !", e);
        }
    }

    @Transactional
    public void loadClassification(String uuid, RetourClassification classification) {

        LocalAuthority localAuthority = localAuthorityRepository.findByUuid(uuid).get();

        List<AttachmentTypeReferencial> attachmentTypeReferencials = classification.getNaturesActes().getNatureActe()
                .stream()
                .map(nature -> new AttachmentTypeReferencial(ActeNature.code(nature.getCodeNatureActe()),
                        nature.getTypePJNatureActe().stream()
                                .map(typePj -> new AttachmentType(typePj.getCodeTypePJ(), typePj.getLibelle()))
                                .collect(Collectors.toSet()),
                        localAuthority))
                .collect(Collectors.toList());

        attachmentTypeReferencials.stream()
                .forEach(attachmentTypeReferencial -> attachmentTypeReferencial.getAttachmentTypes().stream().forEach(
                        attachmentType -> attachmentType.setAttachmentTypeReferencial(attachmentTypeReferencial)));

        localAuthority.getAttachmentTypeReferencials().clear();
        localAuthority.getAttachmentTypeReferencials().addAll(attachmentTypeReferencials);

        List<MaterialCode> codes = new ArrayList<>();
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
    public List<MaterialCode> getCodesMatieres(String uuid) {

        LocalAuthority localAuthority = localAuthorityRepository.findByUuid(uuid).get();
        if (localAuthority.getMaterialCodes() == null || localAuthority.getMaterialCodes().isEmpty()) {
            loadClassification(uuid);
        }
        return localAuthority.getMaterialCodes();
    }

    public Set<AttachmentType> getAttachmentTypeAvailable(ActeNature acteNature, String uuid, String materialCode) {

        // the following code reproduce stela 2 behaviour for attachment type filtering
        String[] materialSplit = materialCode.split("-");

        String materialCode1 = materialSplit[0];
        String materialCode2 = materialSplit[1];

        Set<AttachmentType> attachmentTypes = attachmentTypeRepository
                .findByAttachmentTypeReferencial_acteNatureAndAttachmentTypeReferencial_localAuthorityUuidOrderByLabel(
                        acteNature, uuid);
        attachmentTypes = attachmentTypes.stream().filter(attachmentType -> {

            String firstChar = attachmentType.getCode().substring(0, 1);
            String secondChar = attachmentType.getCode().substring(1, 2);

            return (attachmentType.getCode().startsWith("99"))
                    || ((materialCode1.equals(firstChar) || firstChar.equals("0"))
                            && (materialCode2.equals(secondChar) || secondChar.equals("0")));
        }).collect(Collectors.toSet());
        return attachmentTypes;
    }

    public String getCodeMatiereLabel(String localAuthorityUuid, String codeMatiereKey) {
        Optional<MaterialCode> materialCode = materialCodeRepository.findByCodeAndLocalAuthorityUuid(codeMatiereKey,
                localAuthorityUuid);
        if (materialCode.isPresent())
            return materialCode.get().getLabel();

        return null;
    }

    @Transactional
    public void handleEvent(LocalAuthorityEvent event) throws IOException {
        LocalAuthority localAuthority = localAuthorityRepository.findByUuid(event.getUuid())
                .orElse(new LocalAuthority(event.getUuid(), event.getName(), event.getSiren()));

        localAuthority.setActive(event.getActivatedModules().contains("ACTES"));

        createOrUpdate(localAuthority);

    }

    public boolean localAuthorityGranted(GenericAccount genericAccount, String siren) {
        return genericAccount.getLocalAuthorities().stream()
                .anyMatch(localAuthority -> localAuthority.getActivatedModules().contains("ACTES")
                        && localAuthority.getSiren().equals(siren));
    }

}
