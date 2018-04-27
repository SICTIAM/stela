package fr.sictiam.stela.pesservice.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@ConditionalOnProperty(value = "application.scheduling.enabled")
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
