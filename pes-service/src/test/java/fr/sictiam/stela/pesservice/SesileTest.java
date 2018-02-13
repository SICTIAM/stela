package fr.sictiam.stela.pesservice;

import fr.sictiam.stela.pesservice.model.ui.ClasseurStatus;
import fr.sictiam.stela.pesservice.model.ui.SesileClasseur;
import fr.sictiam.stela.pesservice.model.ui.SesileClasseurRequest;
import fr.sictiam.stela.pesservice.model.ui.SesileServiceOrganisation;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class SesileTest {

    private RestTemplate restTemplate = new RestTemplate();

    String sesileUrl = "https://demo.sesile.fr/";

    @Before
    public void beforeTests() {

        this.restTemplate.setInterceptors(Collections.singletonList((request, body, execution) -> {

            request.getHeaders().add("token", "token_26686e906f40248f284f013e37700091");
            request.getHeaders().add("secret", "secret_52491818dd846ccd4580a16cfd7736dc");
            return execution.execute(request, body);
        }));

    }

    @Test
    public void testAddFileToclasseur() throws Exception {
        InputStream in = new ClassPathResource("data/28000-2017-P-RN-22-1516807373820.xml").getInputStream();

        byte[] targetArray = new byte[in.available()];
        in.read(targetArray);

        ResponseEntity<String> retour = addFileToclasseur(targetArray, "28000-2017-P-RN-22-1516807373820", 2891);
        // {"id":2894,"name":"28000-2017-P-RN-22-1516807373820","repourl":"5a831da0334e6.","type":"application\/xml","signed":false,"histories":[]}
        assertThat(retour, notNullValue());
        assertThat(retour.getStatusCodeValue(), is(HttpStatus.OK.value()));
    }

    @Test
    public void testCheckClasseurStatus() {
        ResponseEntity<SesileClasseur> classeur = checkClasseurStatus(2891);
        assertThat(classeur, notNullValue());
        assertThat(classeur.getStatusCodeValue(), is(HttpStatus.OK.value()));
        assertThat(classeur.getBody().getStatus(), is(ClasseurStatus.IN_PROGRESS));
    }

    @Test
    public void testGetServiceOrga() throws Exception {
        ResponseEntity<SesileServiceOrganisation[]> orgas = getOrga("f.laussinot@sictiam.fr");
        assertThat(orgas, notNullValue());
        assertThat(orgas.getStatusCodeValue(), is(HttpStatus.OK.value()));
    }

    @Test
    public void testPostClasseur() throws Exception {
        ResponseEntity<SesileClasseur> classeur = postClasseur(
                new SesileClasseurRequest("test", "test", "20/02/2018", 2, 1, 3, "f.laussinot@sictiam.fr"));
        assertThat(classeur, notNullValue());
        assertThat(classeur.getStatusCodeValue(), is(HttpStatus.OK.value()));
    }

    public ResponseEntity<String> addFileToclasseur(byte[] file, String fileName, int classeur) throws Exception {

        System.setProperty("javax.net.debug", "all");

        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();

        map.add("name", fileName);
        map.add("filename", fileName);

        ByteArrayResource contentsAsResource = new ByteArrayResource(file) {
            @Override
            public String getFilename() {
                return fileName; // Filename has to be returned in order to be able to post.
            }
        };

        map.add("file", contentsAsResource);
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<LinkedMultiValueMap<String, Object>>(
                map, headers);

        return restTemplate.exchange(sesileUrl + "/api/classeur/" + classeur + "/newDocuments", HttpMethod.POST,
                requestEntity, String.class);
    }

    public ResponseEntity<SesileServiceOrganisation[]> getOrga(String email) throws Exception {

        return restTemplate.getForEntity(sesileUrl + "/api/user/services/" + email, SesileServiceOrganisation[].class);

    }

    public ResponseEntity<SesileClasseur> checkClasseurStatus(int classeur) {
        return restTemplate.getForEntity(sesileUrl + "/api/classeur/" + classeur, SesileClasseur.class);

    }

    public ResponseEntity<SesileClasseur> postClasseur(SesileClasseurRequest classeur) throws Exception {
        return restTemplate.postForEntity(sesileUrl + "/api/classeur/", classeur, SesileClasseur.class);
    }
}
