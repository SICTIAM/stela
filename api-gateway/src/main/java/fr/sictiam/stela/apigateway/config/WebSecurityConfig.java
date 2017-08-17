package fr.sictiam.stela.apigateway.config;

import fr.sictiam.stela.apigateway.config.filter.CsrfTokenGeneratorFilter;
import fr.sictiam.stela.apigateway.config.filter.OzwilloProvisioningFilter;
import org.oasis_eu.spring.config.OasisSecurityConfiguration;
import org.oasis_eu.spring.kernel.security.OasisAuthenticationFilter;
import org.oasis_eu.spring.kernel.security.OpenIdCConfiguration;
import org.oasis_eu.spring.kernel.security.StaticOpenIdCConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Arrays;

@Configuration
public class WebSecurityConfig extends OasisSecurityConfiguration {

    @Value("${application.url}")
    String applicationUrl;

    @Value("${application.security.instanciation_secret}")
    String instanciationSecret;

    @Bean
    @Primary
    public OpenIdCConfiguration openIdCConfiguration() {
        StaticOpenIdCConfiguration configuration = new OpenIdConnectConfiguration();
        configuration.addSkippedPaths(Arrays.asList("/img/", "/js/", "/css/", "/status", "/ozwillo", "/build/"));
        return configuration;
    }

    @Override
    public OasisAuthenticationFilter oasisAuthenticationFilter() throws Exception {
        OasisAuthenticationFilter filter = super.oasisAuthenticationFilter();
        filter.setSuccessHandler(new StelaAuthenticationSuccessHandler(applicationUrl));
        return filter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(oasisAuthenticationFilter(), AbstractPreAuthenticatedProcessingFilter.class)
                .addFilterBefore(new OzwilloProvisioningFilter(instanciationSecret), AbstractPreAuthenticatedProcessingFilter.class)
                .authorizeRequests()
                    .antMatchers("/ozwillo/**").permitAll()
                    .antMatchers("/api/**").authenticated().and()
                .csrf()
                    .ignoringAntMatchers("/ozwillo/**").and()
                .logout()
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout")).logoutSuccessHandler(logoutHandler()).and()
                .exceptionHandling()
                    .authenticationEntryPoint(authenticationEntryPoint()).and()
                .addFilterAfter(oasisExceptionTranslationFilter(authenticationEntryPoint()), ExceptionTranslationFilter.class)
                .addFilterAfter(new CsrfTokenGeneratorFilter(), CsrfFilter.class);
    }
}
