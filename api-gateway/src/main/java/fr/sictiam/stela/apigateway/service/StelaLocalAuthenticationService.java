package fr.sictiam.stela.apigateway.service;

import fr.sictiam.stela.apigateway.model.Agent;
import fr.sictiam.stela.apigateway.util.DiscoveryUtils;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class StelaLocalAuthenticationService implements AuthenticationProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(StelaLocalAuthenticationService.class);


    @Autowired
    DiscoveryUtils discoveryUtils;


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        LOGGER.debug("Authenticating {} with password {}",
                authentication.getPrincipal().toString(),
                authentication.getCredentials() != null ? authentication.getCredentials().toString() : "(none)");

        if (authentication.getCredentials() == null)
            return null;

        List<String> usernames = Arrays.asList("admin-e2e", "test-e2e");

        if (usernames.contains(authentication.getPrincipal().toString())
                && authentication.getPrincipal().toString().equals(authentication.getCredentials().toString())) {
            RestTemplate restTemplate = new RestTemplate();

            String name = (String) authentication.getPrincipal();

            UserInfo userInfo = new UserInfo();
            userInfo.setEmail(name + "@"+ name +".com");
            userInfo.setUserId("123456789" + name);
            userInfo.setFamilyName(name);
            userInfo.setGivenName(name);

            LocalAuthority localAuthoritySICTIAM = restTemplate.getForObject(discoveryUtils.adminServiceUrl() + "/api/admin/local-authority/instance/slug-name/sictiam", LocalAuthority.class);
            if (!Objects.nonNull(localAuthoritySICTIAM)) {
                return null;
            }

            Agent agent = new Agent(userInfo, name.equals("admin-e2e"), localAuthoritySICTIAM.getClientId());

            String token = restTemplate.postForObject(discoveryUtils.adminServiceUrl() + "/api/admin/agent", agent, String.class);

            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = (UsernamePasswordAuthenticationToken) authentication;
            usernamePasswordAuthenticationToken.setDetails(token);
            return new UsernamePasswordAuthenticationToken(agent, token, null);
        }
            throw new BadCredentialsException("Unable to authenticate agent");

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return true;
    }


    private static class LocalAuthority{
        private String uuid;

        private String name;

        private String clientId;

        private String slugName;

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getSlugName() {
            return slugName;
        }

        public void setSlugName(String slugName) {
            this.slugName = slugName;
        }
    }
}

