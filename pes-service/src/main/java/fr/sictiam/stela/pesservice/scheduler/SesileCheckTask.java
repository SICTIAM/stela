package fr.sictiam.stela.pesservice.scheduler;

import fr.sictiam.stela.pesservice.service.SesileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SesileCheckTask {

    @Autowired
    SesileService sesileService;

    @Scheduled(cron = "${application.sesile.cron}")
    public void checkSignature() {
        sesileService.checkPesWithdrawn();
        sesileService.checkPesSigned();
    }

}
