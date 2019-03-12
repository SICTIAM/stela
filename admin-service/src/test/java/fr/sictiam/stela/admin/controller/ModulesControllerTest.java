package fr.sictiam.stela.admin.controller;

import fr.sictiam.stela.admin.config.filter.AuthFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.ResultMatcher.matchAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = ModulesController.class, excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        value = AuthFilter.class))
public class ModulesControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    public void getModules() throws Exception {
        this.mockMvc.perform(get("/api/admin/modules"))
                .andExpect(matchAll(
                        status().isOk(),
                        jsonPath("$").isArray()));
    }
}