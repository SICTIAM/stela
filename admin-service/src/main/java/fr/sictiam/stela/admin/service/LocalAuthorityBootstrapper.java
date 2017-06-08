package fr.sictiam.stela.admin.service;

import fr.sictiam.stela.admin.model.LocalAuthority;
import fr.sictiam.stela.admin.model.Module;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class LocalAuthorityBootstrapper implements CommandLineRunner {

    private final LocalAuthorityService localAuthorityService;

    public LocalAuthorityBootstrapper(LocalAuthorityService localAuthorityService) {
        this.localAuthorityService = localAuthorityService;
    }

    @Override
    public void run(String... args) throws Exception {
        LocalAuthority localAuthority = new LocalAuthority("Mouans Sartoux", "123456789");
        localAuthority = localAuthorityService.create(localAuthority);
        localAuthorityService.addModule(localAuthority.getUuid(), Module.PES);
    }
}
