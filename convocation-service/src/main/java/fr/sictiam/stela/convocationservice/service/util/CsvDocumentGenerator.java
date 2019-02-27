package fr.sictiam.stela.convocationservice.service.util;

import fr.sictiam.stela.convocationservice.model.Convocation;
import fr.sictiam.stela.convocationservice.model.Recipient;
import fr.sictiam.stela.convocationservice.model.ResponseType;

import java.util.HashMap;
import java.util.Map;

public class CsvDocumentGenerator extends DocumentGenerator {


    @Override public byte[] generatePresenceList(Convocation convocation) {

        StringBuilder sb = new StringBuilder();
        sb.append(String.join(";",
                localesService.getMessage("fr", "convocation", "$.convocation.admin.modules.convocation" +
                        ".recipient_config.lastname"),
                localesService.getMessage("fr", "convocation", "$.convocation.admin.modules.convocation" +
                        ".recipient_config.firstname"),
                localesService.getMessage("fr", "convocation", "$.convocation.admin.modules.convocation" +
                        ".recipient_config.email"),
                localesService.getMessage("fr", "convocation", "$.convocation.export.presence"))).append('\n');

        convocation.getRecipientResponses().forEach(recipientResponse -> {
            Recipient r = recipientResponse.getRecipient();

            sb.append(r.getLastname()).append(';')
                    .append(r.getFirstname()).append(';')
                    .append(r.getEmail()).append(';');

            if (recipientResponse.getResponseType() == ResponseType.SUBSTITUTED && recipientResponse.getSubstituteRecipient() != null) {
                Map<String, String> params = new HashMap<String, String>() {
                    {
                        put("substitute",
                                recipientResponse.getSubstituteRecipient().getFirstname() + " " + recipientResponse.getSubstituteRecipient().getLastname());
                    }
                };
                sb.append(localesService.getMessage("fr", "convocation",
                        "$.convocation.export." + recipientResponse.getResponseType(), params)).append('\n');
            } else {
                sb.append(localesService.getMessage("fr", "convocation",
                        "$.convocation.export." + recipientResponse.getResponseType())).append('\n');
            }

        });

        return sb.toString().getBytes();
    }
}