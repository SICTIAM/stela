package fr.sictiam.stela.apigateway.config;

import fr.sictiam.stela.apigateway.service.StelaLocalAuthenticationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@Profile(value = "test-e2e")
public class LocalWebSecurity extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/img/", "/js/", "/css/", "/status", "/api/admin/ozwillo", "/build/",
                        "/editeur/api/acte", "/api/pes/sesile/signature-hook", "/api/pes/actuator", "/api/convocation").permitAll()
                .antMatchers("/api/admin/**", "/ws/admin/import/**").authenticated()
                .and()
                .formLogin()
                .and()
                .logout()
                .deleteCookies("stela", "XSRF-TOKEN", "JSESSIONID")
                .clearAuthentication(true)
                .invalidateHttpSession(true)
                .and()
                .exceptionHandling()
                .and()
                .httpBasic()
                .and().csrf().disable();
    }

    @Bean
    public StelaLocalAuthenticationService springAuthenticationProvider() {
        return new StelaLocalAuthenticationService();
    }

}
