package fr.sictiam.stela.pesservice.service;

import fr.sictiam.stela.pesservice.dao.PesRetourRepository;
import fr.sictiam.stela.pesservice.model.Attachment;
import fr.sictiam.stela.pesservice.model.LocalAuthority;
import fr.sictiam.stela.pesservice.model.PesRetour;
import fr.sictiam.stela.pesservice.model.util.TarGzUtils;
import fr.sictiam.stela.pesservice.soap.model.paull.GetTabPESRetourStruct;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.xml.security.utils.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PesRetourService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesRetourService.class);

    private final PesRetourRepository pesRetourRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public PesRetourService(PesRetourRepository pesRetourRepository) {
        this.pesRetourRepository = pesRetourRepository;
    }

    public PesRetour getByUuid(String uuid) {
        return pesRetourRepository.findByUuid(uuid).get();
    }

    public List<PesRetour> getAllWithQuery(String multifield, String filename, LocalDate creationFrom,
            LocalDate creationTo, String currentLocalAuthUuid, Integer limit, Integer offset) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<PesRetour> query = builder.createQuery(PesRetour.class);
        Root<PesRetour> pesRetourRoot = query.from(PesRetour.class);

        List<Predicate> predicates = getQueryPredicates(builder, pesRetourRoot, multifield, filename, creationFrom,
                creationTo, currentLocalAuthUuid);
        query.where(predicates.toArray(new Predicate[predicates.size()]))
                .orderBy(builder.desc(pesRetourRoot.get("creation")));

        return entityManager.createQuery(query).setFirstResult(offset).setMaxResults(limit).getResultList();
    }

    public Long countAllWithQuery(String multifield, String filename, LocalDate creationFrom, LocalDate creationTo,
            String currentLocalAuthUuid) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<PesRetour> pesRetourRoot = query.from(PesRetour.class);

        List<Predicate> predicates = getQueryPredicates(builder, pesRetourRoot, multifield, filename, creationFrom,
                creationTo, currentLocalAuthUuid);
        query.select(builder.count(pesRetourRoot));
        query.where(predicates.toArray(new Predicate[predicates.size()]));

        return entityManager.createQuery(query).getSingleResult();
    }

    private List<Predicate> getQueryPredicates(CriteriaBuilder builder, Root<PesRetour> pesRetourRoot,
            String multifield, String filename, LocalDate creationFrom, LocalDate creationTo,
            String currentLocalAuthUuid) {
        List<Predicate> predicates = new ArrayList<>();
        if (StringUtils.isNotBlank(multifield)) {
            Join<Attachment, PesRetour> attachmentJoin = pesRetourRoot.join("attachment");
            attachmentJoin.on(
                    builder.like(builder.lower(attachmentJoin.get("filename")), "%" + multifield.toLowerCase() + "%"));
        }
        if (StringUtils.isNotBlank(filename)) {
            Join<Attachment, PesRetour> attachmentJoin = pesRetourRoot.join("attachment");
            attachmentJoin.on(
                    builder.like(builder.lower(attachmentJoin.get("filename")), "%" + filename.toLowerCase() + "%"));
        }
        if (StringUtils.isNotBlank(currentLocalAuthUuid)) {
            Join<LocalAuthority, PesRetour> localAuthorityJoin = pesRetourRoot.join("localAuthority");
            localAuthorityJoin.on(builder.equal(localAuthorityJoin.get("uuid"), currentLocalAuthUuid));
        }
        if (creationFrom != null && creationTo != null) {
            predicates.add(builder.and(builder.between(pesRetourRoot.get("creation"), creationFrom.atStartOfDay(),
                    creationTo.plusDays(1).atStartOfDay())));
        }
        return predicates;
    }

    public List<Map<String, String>> getUncollectedLocalAuthorityPesRetours(String siren) {
        List<PesRetour> pesRetours = pesRetourRepository.findByLocalAuthoritySirenAndCollectedFalse(siren);

        List<Map<String, String>> outputList = pesRetours.stream().map(pesRetour -> {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);
                TarGzUtils.addEntry(pesRetour.getAttachment().getFilename(), pesRetour.getAttachment().getFile(), taos);

                taos.close();
                baos.close();

                ByteArrayOutputStream archive = TarGzUtils.compress(baos);
                String archiveName = pesRetour.getAttachment().getFilename() + ".tar.gz";
                String archiveBase64 = Base64.encode(archive.toByteArray());
                Map<String, String> returnMap = new HashMap<>();
                returnMap.put("chaine_archive", archiveBase64);
                returnMap.put("filename", archiveName);
                return returnMap;

            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
            return null;
        }).filter(pesRetourMap -> pesRetourMap != null).collect(Collectors.toList());
        return outputList;
    }

    public List<GetTabPESRetourStruct> getPaullUncollectedLocalAuthorityPesRetours(String siren) {
        List<PesRetour> pesRetours = pesRetourRepository.findByLocalAuthoritySirenAndCollectedFalse(siren);

        List<GetTabPESRetourStruct> outputList = pesRetours.stream().map(pesRetour -> {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);
                TarGzUtils.addEntry(pesRetour.getAttachment().getFilename(), pesRetour.getAttachment().getFile(), taos);

                taos.close();
                baos.close();

                ByteArrayOutputStream archive = TarGzUtils.compress(baos);
                String archiveName = pesRetour.getAttachment().getFilename() + ".tar.gz";
                String archiveBase64 = Base64.encode(archive.toByteArray());
                GetTabPESRetourStruct tabPESRetourStruct = new GetTabPESRetourStruct();
                tabPESRetourStruct.setChaineArchive(archiveBase64);
                tabPESRetourStruct.setFilename(archiveName);
                return tabPESRetourStruct;

            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
            return null;
        }).filter(pesRetourMap -> pesRetourMap != null).collect(Collectors.toList());
        return outputList;
    }

    public PesRetour collect(String filename) {
        Optional<PesRetour> pesRetourOpt = pesRetourRepository
                .findByAttachmentFilename(filename.replace(".tar.gz", ""));
        PesRetour pesRetour = pesRetourOpt.orElse(null);
        if (pesRetourOpt.isPresent()) {
            pesRetour.setCollected(true);
            pesRetour = pesRetourRepository.save(pesRetour);
        }
        return pesRetour;
    }
}
