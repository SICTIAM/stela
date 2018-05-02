package fr.sictiam.stela.admin.soap.endpoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.admin.model.GenericAccount;
import fr.sictiam.stela.admin.model.LocalAuthority;
import fr.sictiam.stela.admin.model.PaullConnection;
import fr.sictiam.stela.admin.service.GenericAccountService;
import fr.sictiam.stela.admin.service.LocalAuthorityService;
import fr.sictiam.stela.admin.service.PaullConnectionService;
import fr.sictiam.stela.admin.soap.model.LoginResponse;
import fr.sictiam.stela.admin.soap.model.PaullSoapToken;
import fr.sictiam.stela.admin.soap.model.loginRequest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpServletConnection;

import javax.servlet.http.HttpServletRequest;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Optional;

@Endpoint
public class LoginEndPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginEndPoint.class);

    private static final String NAMESPACE_URI = "http://www.processmaker.com";

    private final GenericAccountService genericAccountService;
    private final LocalAuthorityService localAuthorityService;
    private final PaullConnectionService paullConnectionService;
    private final PasswordEncoder passwordEncoder;
    private static SecureRandom random = new SecureRandom();

    protected HttpServletRequest getHttpServletRequest() {
        TransportContext ctx = TransportContextHolder.getTransportContext();
        return (null != ctx) ? ((HttpServletConnection) ctx.getConnection()).getHttpServletRequest() : null;
    }

    @Value("${application.jwt.secret}")
    String SECRET;

    @Value("${application.jwt.expire}")
    long EXPIRATIONTIME;

    public LoginEndPoint(GenericAccountService genericAccountService, PasswordEncoder passwordEncoder,
            LocalAuthorityService localAuthorityService, PaullConnectionService paullConnectionService) {
        this.genericAccountService = genericAccountService;
        this.passwordEncoder = passwordEncoder;
        this.localAuthorityService = localAuthorityService;
        this.paullConnectionService = paullConnectionService;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "login")
    public @ResponsePayload LoginResponse login(@RequestPayload loginRequest loginInput) {
        LoginResponse loginOutput = new LoginResponse();
        Optional<GenericAccount> genericAccount = genericAccountService.getByEmail(loginInput.getUserid());
        HttpServletRequest request = getHttpServletRequest();

        String requestURI = request.getRequestURI();

        String siren = StringUtils.removeStart(requestURI.split("/")[2], "sys");

        Optional<LocalAuthority> localAuthority = localAuthorityService.findBySiren(siren);
        if (localAuthority.isPresent()) {
            if (genericAccount.isPresent()
                    && passwordEncoder.matches(loginInput.getPassword(), genericAccount.get().getPassword())) {

                if (genericAccount.get().getLocalAuthorities().stream()
                        .anyMatch(localAuth -> localAuth.getSiren().equals(siren))) {

                    loginOutput.setStatusCode("OK");
                    PaullSoapToken paullSoapToken = new PaullSoapToken(genericAccount.get().getUuid(), siren,
                            localAuthority.get().getActivatedModules());
                    ObjectMapper mapper = new ObjectMapper();
                    String body;
                    try {
                        body = mapper.writeValueAsString(paullSoapToken);
                    } catch (JsonProcessingException e) {
                        LOGGER.error(e.getMessage());
                        loginOutput.setStatusCode("NOK");
                        loginOutput.setMessage("INTERNAL ERROR");
                        return loginOutput;
                    }
                    String jwtToken = Jwts.builder().setSubject(body)
                            .setExpiration(new Date(System.currentTimeMillis() + EXPIRATIONTIME))
                            .signWith(SignatureAlgorithm.HS512, SECRET).compact();
                    BigInteger bigInteger = new BigInteger(130, random);

                    String sessionId = String.valueOf(bigInteger.toString(32));
                    paullConnectionService.save(new PaullConnection(sessionId, jwtToken));
                    loginOutput.setMessage(sessionId);
                    loginOutput.setVersion("2.0");
                    loginOutput.setTimestamp(System.currentTimeMillis() + "");
                } else {
                    loginOutput.setStatusCode("NOK");
                    loginOutput.setMessage("LocalAuthority not granted");
                }

            } else {
                loginOutput.setStatusCode("NOK");
                loginOutput.setMessage("Wrong User id or password");
            }
        } else {
            loginOutput.setStatusCode("NOK");
            loginOutput.setMessage("LocalAuthority not found");
        }

        return loginOutput;
    }

}