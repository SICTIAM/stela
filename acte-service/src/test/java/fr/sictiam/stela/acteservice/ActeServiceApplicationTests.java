package fr.sictiam.stela.acteservice;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import fr.sictiam.stela.acteservice.dao.ActeRepository;
import fr.sictiam.stela.acteservice.model.Acte;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Hashtable;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ActeServiceApplication.class)
@WebAppConfiguration
public class ActeServiceApplicationTests {


    private MockMvc mockMvc;
	
    @Autowired
    private WebApplicationContext webApplicationContext;
	
    @Autowired
    private ActeRepository repository;
	
    private String[] numeros = new String[] { "numero1", "numero2" };   
    private Hashtable<String, Long> ids = new Hashtable<String, Long>();

    @Before
    public void setup() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();

        this.repository.deleteAllInBatch();

        for(String numero: this.numeros){
            Acte inserted = this.repository.save(new Acte(numero));
            ids.put(inserted.getNumero(), inserted.getUuid());
        }
    }

    @Test
    public void acteNotFoundById() throws Exception {
        mockMvc.perform(get("/acte/id/56")).andExpect(status().isNotFound());
    }

    @Test
    public void acteNotFoundByNumero() throws Exception {
        mockMvc.perform(get("/acte/numero/numero3")).andExpect(status().isNotFound());
    }

	// TODO : remove code duplication
    @Test
    public void acteFoundById() throws Exception {
        for (Map.Entry<String, Long> entry : this.ids.entrySet()) {
            mockMvc.perform(get("/acte/id/" + entry.getValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero", is(entry.getKey())))
                .andExpect(jsonPath("$.uuid", is(entry.getValue().intValue())));
        };
    }

    @Test
    public void acteFoundByNumero() throws Exception {
	    for (Map.Entry<String, Long> entry : this.ids.entrySet()) {
            mockMvc.perform(get("/acte/numero/" + entry.getKey()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero", is(entry.getKey())))
                .andExpect(jsonPath("$.uuid", is(entry.getValue().intValue())));
        };
    }
}
