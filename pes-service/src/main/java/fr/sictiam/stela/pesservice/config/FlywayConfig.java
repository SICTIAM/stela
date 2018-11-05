package fr.sictiam.stela.pesservice.config;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration.FlywayConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ResourceLoader;

import javax.sql.DataSource;

import java.util.List;

@Configuration
class FlywayConfig extends FlywayConfiguration {


    public FlywayConfig(FlywayProperties properties,
            DataSourceProperties dataSourceProperties, ResourceLoader resourceLoader,
            ObjectProvider<DataSource> dataSource,
            @FlywayDataSource ObjectProvider<DataSource> flywayDataSource,
            ObjectProvider<FlywayMigrationStrategy> migrationStrategy,
            ObjectProvider<List<FlywayCallback>> flywayCallbacks) {

        super(properties, dataSourceProperties, resourceLoader, dataSource, flywayDataSource, migrationStrategy, flywayCallbacks);
    }

    @Override @Primary
    @Bean(name = "flywayInitializer")
    @DependsOn("springUtility")
    public FlywayMigrationInitializer flywayInitializer(Flyway flyway) {
        return super.flywayInitializer(flyway);
        //return new FlywayMigrationInitializer(flyway, null);
    }
}