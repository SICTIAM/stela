package fr.sictiam.stela.apigateway.config.filter;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class OzwilloProvisioningFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(OzwilloProvisioningFilter.class);
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    private String instanciationSecret;

    public OzwilloProvisioningFilter(String instanciationSecret) {
        this.instanciationSecret = instanciationSecret;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return  !request.getServletPath().startsWith("/ozwillo");
    }

    // currently tested on command line with :
    // http --verbose --json POST http://localhost:9004/ozwillo/instance 'X-Hub-Signature: sha1=358a8b9f467eeab55dd203af20fa7305a9327f15' instance_id=stela_sictiam client_id=stela
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        LOGGER.debug("Verifying authentication of an Ozwillo provisioning request");
        String signatureHeader = request.getHeader("X-Hub-Signature");
        if (signatureHeader == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "No X-Hub-Signature header found in request");
            return;
        } else if (!signatureHeader.startsWith("sha1=")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "sha1 algo expected in signature");
            return;
        }

        String receivedHmac = signatureHeader.split("=")[1];
        LOGGER.debug("Received hmac : {}", receivedHmac);
        SecretKeySpec signingKey = new SecretKeySpec(instanciationSecret.getBytes(), HMAC_SHA1_ALGORITHM);
        String computedHmac;
        try {
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);
            String requestBody = IOUtils.toString(request.getReader());
            LOGGER.debug("Received request body is {}", requestBody);
            computedHmac = Hex.encodeHexString(mac.doFinal(requestBody.getBytes()));
            // set the body in a request attribute as it can't be read twice from the request
            request.setAttribute("provisioningRequest", requestBody);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Err, sha1 is not available ?!");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "sha1 algo is not available");
            return;
        } catch (InvalidKeyException e) {
            LOGGER.error("Err, how can my key be invalid ?!");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid key");
            return;
        }

        LOGGER.debug("Computed HMAC : {}", computedHmac);
        if (!receivedHmac.equals(computedHmac)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Provided HMAC does not conform to what was expected : " + receivedHmac);
        }

        filterChain.doFilter(request, response);
    }
}
