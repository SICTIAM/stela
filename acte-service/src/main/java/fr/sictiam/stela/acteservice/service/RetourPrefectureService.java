package fr.sictiam.stela.acteservice.service;

import fr.sictiam.stela.acteservice.dao.ActeHistoryRepository;
import fr.sictiam.stela.acteservice.dao.ActeRepository;
import fr.sictiam.stela.acteservice.dao.LocalAuthorityRepository;
import fr.sictiam.stela.acteservice.model.*;
import fr.sictiam.stela.acteservice.model.event.ActeHistoryEvent;
import fr.sictiam.stela.acteservice.model.xml.*;
import fr.sictiam.stela.acteservice.service.exceptions.ActeNotFoundException;
import fr.sictiam.stela.acteservice.service.util.ZipGeneratorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RetourPrefectureService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetourPrefectureService.class);

    private final ActeRepository acteRepository;
    private final ActeHistoryRepository acteHistoryRepository;
    private final LocalAuthorityRepository localAuthorityRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ZipGeneratorUtil zipGeneratorUtil;

    public RetourPrefectureService(ActeRepository acteRepository, ActeHistoryRepository acteHistoryRepository,
                                   LocalAuthorityRepository localAuthorityRepository, ApplicationEventPublisher applicationEventPublisher, ZipGeneratorUtil zipGeneratorUtil) {
        this.acteRepository = acteRepository;
        this.acteHistoryRepository = acteHistoryRepository;
        this.localAuthorityRepository = localAuthorityRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.zipGeneratorUtil = zipGeneratorUtil;
    }

    public void receiveARActe(ARActe arActe, Attachment attachment) {
        Acte acte = getByMiatId(arActe.getIDActe());
        publishActeHistory(acte.getUuid(), StatusType.ACK_RECEIVED, attachment, Optional.empty(), Optional.empty());
    }

    public void receiveARAnnulation(ARAnnulation arAnnulation, Attachment attachment) {
        Acte acte = getByMiatId(arAnnulation.getIDActe());
        publishActeHistory(acte.getUuid(), StatusType.CANCELLED, attachment, Optional.empty(), Optional.empty());
    }

    public void receiveAnomalieActe(EnveloppeMISILLCL enveloppeMISILLCL, AnomalieActe anomalieActe, Attachment attachment) {
        LocalAuthority localAuthority = localAuthorityRepository.findBySiren(enveloppeMISILLCL.getDestinataire().getSIREN())
                .orElseThrow(ActeNotFoundException::new);
        Acte acte = acteRepository.findFirstByNumberAndDecisionAndNatureAndLocalAuthority_UuidAndDraftNull(
                anomalieActe.getActeRecu().getNumeroInterne(),
                anomalieActe.getActeRecu().getDate(),
                ActeNature.code(anomalieActe.getActeRecu().getCodeNatureActe()),
                localAuthority.getUuid())
                .orElseThrow(ActeNotFoundException::new);
        publishActeHistory(acte.getUuid(), StatusType.NACK_RECEIVED, attachment,
                Optional.of(anomalieActe.getDetail()), Optional.empty());
    }

    public void receiveAnomalieEnveloppe(AnomalieEnveloppe anomalieEnveloppe, Attachment attachment) {
        String[] splited = attachment.getFilename().split("--");
        splited = Arrays.copyOfRange(splited, 1, splited.length);
        String sourceTarGz = String.join("--", splited).replace(".xml", ".tar.gz");
        ActeHistory acteHistorySource = acteHistoryRepository.findFirstByFileNameContaining(sourceTarGz)
                .orElseThrow(ActeNotFoundException::new);
        Acte acte = acteRepository.findByUuidAndDraftNull(acteHistorySource.getActeUuid())
                .orElseThrow(ActeNotFoundException::new);
        publishActeHistory(acte.getUuid(), StatusType.NACK_RECEIVED, attachment,
                Optional.of(anomalieEnveloppe.getDetail()), Optional.empty());
    }

    public void receiveCourrierSimple(CourrierSimple courrierSimple, Attachment attachment) {
        Acte acte = getByMiatId(courrierSimple.getIDActe());
        publishActeHistory(acte.getUuid(), StatusType.COURRIER_SIMPLE_RECEIVED, attachment,
                Optional.empty(), Optional.of(Flux.COURRIER_SIMPLE));
    }

    public void receiveDemandePieceComplementaire(DemandePieceComplementaire demandePieceComplementaire, Attachment attachment) {
        Acte acte = getByMiatId(demandePieceComplementaire.getIDActe());
        publishActeHistory(acte.getUuid(), StatusType.DEMANDE_PIECE_COMPLEMENTAIRE_RECEIVED, attachment,
                Optional.of(demandePieceComplementaire.getDescriptionPieces()), Optional.of(Flux.DEMANDE_PIECE_COMPLEMENTAIRE));
    }

    public void receiveLettreObservations(LettreObservations lettreObservations, Attachment attachment) {
        Acte acte = getByMiatId(lettreObservations.getIDActe());
        publishActeHistory(acte.getUuid(), StatusType.LETTRE_OBSERVATION_RECEIVED, attachment,
                Optional.of(lettreObservations.getMotif()), Optional.of(Flux.LETTRE_OBSERVATION));
    }

    public void receiveDefere(DefereTA defereTA, List<Attachment> attachments)
            throws IOException {
        Acte acte = getByMiatId(defereTA.getIDActe());
        byte[] file;
        String fileName;
        if (attachments.size() == 1) {
            file = attachments.get(0).getFile();
            fileName = attachments.get(0).getFilename();
        } else {
            fileName = "DefereTA_" + defereTA.getIDActe() + ".zip";
            file = zipGeneratorUtil.createZip(
                    attachments.stream().collect(Collectors.toMap(Attachment::getFilename, Attachment::getFile)));
        }
        publishActeHistory(acte.getUuid(), StatusType.DEFERE_RECEIVED, new Attachment(file, fileName, file.length),
                Optional.of(defereTA.getNatureIllegalite()), Optional.empty());
    }

    public void receiveARPieceComplementaire(ARReponseCL arReponseCL, Attachment attachment) {
        Acte acte = getByMiatId(arReponseCL.getInfosCourrierPref().getIDActe());
        publishActeHistory(acte.getUuid(), StatusType.ACK_REPONSE_PIECE_COMPLEMENTAIRE, attachment, Optional.empty(), Optional.empty());
    }

    public void receiveARReponseRejetLettreObservations(ARReponseCL arReponseCL, Attachment attachment) {
        Acte acte = getByMiatId(arReponseCL.getInfosCourrierPref().getIDActe());
        publishActeHistory(acte.getUuid(), StatusType.ACK_REPONSE_LETTRE_OBSERVATION, attachment, Optional.empty(), Optional.empty());
    }

    void publishActeHistory(String acteUuid, StatusType statusType, Attachment attachment,
                                    Optional<String> message, Optional<Flux> flux) {
        ActeHistory acteHistory = new ActeHistory(acteUuid, statusType, LocalDateTime.now(),
                attachment.getFile(), attachment.getFilename());
        message.ifPresent(acteHistory::setMessage);
        flux.ifPresent(acteHistory::setFlux);
        applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));
        LOGGER.info("Published event {} for acte {}", statusType, acteUuid);
    }

    private Acte getByMiatId(String miatId) {
        return acteRepository.findByMiatId(miatId).orElseThrow(ActeNotFoundException::new);
    }
}
