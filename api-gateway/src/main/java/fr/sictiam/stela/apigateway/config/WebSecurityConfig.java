package fr.sictiam.stela.apigateway.config;

import fr.sictiam.stela.apigateway.config.filter.CsrfTokenGeneratorFilter;
import fr.sictiam.stela.apigateway.util.DiscoveryUtils;
import org.oasis_eu.spring.config.OasisSecurityConfiguration;
import org.oasis_eu.spring.kernel.security.OasisAuthenticationFilter;
import org.oasis_eu.spring.kernel.security.OpenIdCConfiguration;
import org.oasis_eu.spring.kernel.security.StaticOpenIdCConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    DiscoveryUtils discoveryUtils;

    @Bean
    @Primary
    public OpenIdCConfiguration openIdCConfiguration() {
        StaticOpenIdCConfiguration configuration = new OpenIdConnectConfiguration();
        configuration
                .addSkippedPaths(Arrays.asList("/img/", "/js/", "/css/", "/status", "/api/admin/ozwillo", "/build/",
                        "/editeur/api/acte", "/api/pes/sesile/signature-hook", "/api/pes/actuator", "/api/convocation"));
        return configuration;
    }

    @Override
    public OasisAuthenticationFilter oasisAuthenticationFilter() throws Exception {
        OasisAuthenticationFilter filter = super.oasisAuthenticationFilter();
        filter.setSuccessHandler(new StelaAuthenticationSuccessHandler(applicationUrlWithSlug, discoveryUtils));
        return filter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.addFilterBefore(oasisAuthenticationFilter(), AbstractPreAuthenticatedProcessingFilter.class)
                .authorizeRequests()
                    .regexMatchers("/api/convocation.*(\\?|&)token=\\w+.*").permitAll()
                    .antMatchers("/api/pes/sesile/signature-hook/**").permitAll()
                    .antMatchers("/api/acte/public/**").permitAll()
                    .antMatchers("/api/admin/instance/**").permitAll()
                    .antMatchers("/api/admin/local-authority/all").permitAll()
                    .antMatchers("/api/admin/ozwillo/**").permitAll()
                    .antMatchers("/api/api-gateway/isMainDomain").permitAll()
                    .antMatchers("/api/api-gateway/loginWithSlug/**").permitAll()
                    .antMatchers("/api/*/actuator/**").permitAll()
                    .antMatchers("/api/*/locales/**").permitAll()
                    .antMatchers("/api/*/ws/**").permitAll()
                    .antMatchers("/api/**").authenticated()
                    .antMatchers("/externalws/**").permitAll()
                    .antMatchers("/public/**").permitAll()
                    .and()
                .csrf()
                    .ignoringAntMatchers("/api/admin/ozwillo/**")
                    .ignoringAntMatchers("/api/*/actuator/**")
                    .ignoringAntMatchers("/api/*/ws/**")
                    .ignoringAntMatchers("/editeur/api/**")
                    .ignoringAntMatchers("/api/pes/sesile/signature-hook/**")
                    .ignoringAntMatchers("/api/convocation/**")
                    .ignoringAntMatchers("/externalws/**")
                    .and()
                .logout()
                    // session is invalidated by OasisLogoutHandler as per https://doc.ozwillo.com/#s4-sign-out
                    // what's more token revocation in OasisLogoutHandler fails if session is invalidated before
                    .invalidateHttpSession(false)
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                    .logoutSuccessHandler(logoutHandler())
                    .and()
                .exceptionHandling()
                    .authenticationEntryPoint(authenticationEntryPoint())
                    .and()
                .addFilterAfter(oasisExceptionTranslationFilter(authenticationEntryPoint()), ExceptionTranslationFilter.class)
                .addFilterAfter(new CsrfTokenGeneratorFilter(), CsrfFilter.class);
    }
}
