package fr.sictiam.stela.convocationservice.service.util;

import fr.sictiam.stela.convocationservice.model.Convocation;
import fr.sictiam.stela.convocationservice.model.Recipient;
import fr.sictiam.stela.convocationservice.model.ResponseType;
import fr.sictiam.stela.convocationservice.model.csv.PresenceBean;
import fr.sictiam.stela.convocationservice.service.LocalesService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvDocumentGenerator implements DocumentGenerator {

    private final static Logger LOGGER = LoggerFactory.getLogger(CsvDocumentGenerator.class);

    private LocalesService localesService;

    public CsvDocumentGenerator() {
        localesService = new LocalesService();
    }

    @Override public byte[] generatePresenceList(Convocation convocation) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8));

        ICsvBeanWriter writer = null;

        try {
            bw.write('\uFEFF'); // BOM for UTF-*
            writer = new CsvBeanWriter(bw, CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);

            String[] headers = new String[]{
                    localesService.getMessage("fr", "convocation", "$.convocation.admin.modules.convocation" +
                            ".recipient_config.lastname"),
                    localesService.getMessage("fr", "convocation", "$.convocation.admin.modules.convocation" +
                            ".recipient_config.firstname"),
                    localesService.getMessage("fr", "convocation", "$.convocation.export.pdf.localAuthority"),
                    localesService.getMessage("fr", "convocation", "$.convocation.admin.modules.convocation" +
                            ".recipient_config.email"),
                    localesService.getMessage("fr", "convocation", "$.convocation.export.presence"),
                    localesService.getMessage("fr", "convocation", "$.convocation.export.guest")
            };

            final List<PresenceBean> beans = new ArrayList<>();

            writer.writeHeader(headers);
            convocation.getRecipientResponses().forEach(recipientResponse -> {
                Recipient r = recipientResponse.getRecipient();
                String presence;
                String epci = StringUtils.isNotBlank(r.getEpciName()) ? r.getEpciName() : "";
                if (recipientResponse.getResponseType() == ResponseType.SUBSTITUTED && recipientResponse.getSubstituteRecipient() != null) {
                    Map<String, String> params = new HashMap<String, String>() {
                        {
                            put("substitute",
                                    recipientResponse.getSubstituteRecipient().getFirstname() + " " + recipientResponse.getSubstituteRecipient().getLastname());
                        }
                    };
                    presence = localesService.getMessage("fr", "convocation",
                            "$.convocation.export." + recipientResponse.getResponseType(), params);
                } else {
                    presence = localesService.getMessage("fr", "convocation",
                            "$.convocation.export." + recipientResponse.getResponseType());
                }

                beans.add(new PresenceBean(r.getLastname(), r.getFirstname(), epci, r.getEmail(), presence,
                        recipientResponse.isGuest() ? localesService.getMessage("fr", "convocation",
                                "$.convocation.export.yes") : ""));
            });

            for (PresenceBean bean : beans) {
                writer.write(bean, PresenceBean.fields());
            }

            writer.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            LOGGER.error("Failed to write CSV content: {}", e.getMessage());
            return new byte[0];
        } finally {
            try {
                if (writer != null) writer.close();
                bw.close();
                baos.close();
            } catch (IOException e) {
                LOGGER.error("Failed to close stream and/or writer: {}", e.getMessage());
            }
        }
    }
}

