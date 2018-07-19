package fr.sictiam.stela.admin.service;

import fr.sictiam.stela.admin.dao.InstanceRepository;
import fr.sictiam.stela.admin.model.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InstanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceService.class);

    private final InstanceRepository instanceRepository;

    public InstanceService(InstanceRepository instanceRepository) {
        this.instanceRepository = instanceRepository;
    }

    public String getWelcomeMessage() {
        return getInstance().getWelcomeMessage();
    }

    public String getLegalNotice() {
        return getInstance().getLegalNotice();
    }

    public Instance getInstance() {
        return instanceRepository.findAll().get(0);
    }

    public void updateInstance(Instance instance) {
        instanceRepository.save(instance);
    }

}
