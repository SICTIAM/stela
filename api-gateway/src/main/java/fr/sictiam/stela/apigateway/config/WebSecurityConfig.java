package fr.sictiam.stela.apigateway.config;

import fr.sictiam.stela.apigateway.config.filter.CsrfTokenGeneratorFilter;
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

    @Value("${application.urlWithSlug}")
    String applicationUrlWithSlug;

    @Bean
    @Primary
    public OpenIdCConfiguration openIdCConfiguration() {
        StaticOpenIdCConfiguration configuration = new OpenIdConnectConfiguration();
        configuration.addSkippedPaths(Arrays.asList("/img/", "/js/", "/css/", "/status", "/api/admin/ozwillo", "/build/"));
        return configuration;
    }

    @Override
    public OasisAuthenticationFilter oasisAuthenticationFilter() throws Exception {
        OasisAuthenticationFilter filter = super.oasisAuthenticationFilter();
        filter.setSuccessHandler(new StelaAuthenticationSuccessHandler(applicationUrlWithSlug));
        return filter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(oasisAuthenticationFilter(), AbstractPreAuthenticatedProcessingFilter.class)
                .authorizeRequests()
                    .antMatchers("/api/admin/local-authority/all-basic").permitAll()
                    .antMatchers("/api/admin/ozwillo/**").permitAll()
                    .antMatchers("/api/api-gateway/isLocalAuthorityInstance").permitAll()
                    .antMatchers("/api/api-gateway/loginWithSlug/**").permitAll()
                    .antMatchers("/api/*/locales/**").permitAll()
                    .antMatchers("/api/**").authenticated().and()
                .csrf()
                    .ignoringAntMatchers("/api/admin/ozwillo/**").and()
                .logout()
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout")).logoutSuccessHandler(logoutHandler()).and()
                .exceptionHandling()
                    .authenticationEntryPoint(authenticationEntryPoint()).and()
                .addFilterAfter(oasisExceptionTranslationFilter(authenticationEntryPoint()), ExceptionTranslationFilter.class)
                .addFilterAfter(new CsrfTokenGeneratorFilter(), CsrfFilter.class);
    }
}
