package fr.sictiam.stela.admin.controller;

import fr.sictiam.stela.admin.config.filter.AuthFilter;
import fr.sictiam.stela.admin.model.LocalAuthority;
import fr.sictiam.stela.admin.model.OzwilloInstanceInfo;
import fr.sictiam.stela.admin.model.TokenResponse;
import fr.sictiam.stela.admin.service.LocalAuthorityService;
import fr.sictiam.stela.admin.service.ProfileService;
import fr.sictiam.stela.admin.service.WorkGroupService;
import fr.sictiam.stela.admin.service.exceptions.NotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.ResultMatcher.matchAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = LocalAuthorityController.class, excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        value = AuthFilter.class))
public class LocalAuthorityControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private LocalAuthorityController localAuthorityController;

    @MockBean
    private LocalAuthorityService localAuthorityService;

    @MockBean
    private ProfileService profileService;

    @MockBean
    private WorkGroupService workGroupService;

    @Test
    public void getAccessToken() throws Exception {
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken("test-access-token");
        tokenResponse.setTokenType("Bearer");
        tokenResponse.setIdToken("test-id-token");

        given(localAuthorityService.getAccessTokenFromKernel("uuid-test-one")).willReturn(Optional.of(tokenResponse));


        ResultActions resultActions = mockMvc.perform(get("/api/admin/local-authority/uuid-test-one/accessToken"));

        resultActions
                .andExpect(matchAll(
                        status().isOk(),
                        jsonPath("$").value("test-access-token")
                ))
                .andDo(print());
    }

    @Test
    public void getAccessTokenFailed() throws Exception {
        given(localAuthorityService.getAccessTokenFromKernel("uuid-test-one")).willReturn(Optional.empty());


        ResultActions resultActions = mockMvc.perform(get("/api/admin/local-authority/uuid-test-one/accessToken"));

        resultActions
                .andExpect(status().isBadRequest())
                .andDo(print());
    }



    @Test
    public void getDcId() throws Exception {
        OzwilloInstanceInfo ozwilloInstanceInfo = new OzwilloInstanceInfo();
        ReflectionTestUtils.setField(ozwilloInstanceInfo, "dcId", "https://data.sictiam.fr/dc/type/test:Test_0");

        LocalAuthority localAuthority = new LocalAuthority("uuid-local-authority-test-one");
        localAuthority.setOzwilloInstanceInfo(ozwilloInstanceInfo);

        given(localAuthorityService.getByUuid("uuid-local-authority-test-one")).willReturn(localAuthority);


        ResultActions resultActions = mockMvc.perform(get("/api/admin/local-authority/uuid-local-authority-test-one/dcId"));

        resultActions
                .andExpect(matchAll(
                        status().isOk(),
                        jsonPath("$").value("https://data.sictiam.fr/dc/type/test:Test_0")
                ))
                .andDo(print());
    }

    @Test
    public void getDcIdNotFoundException() throws Exception {
        given(localAuthorityService.getByUuid("uuid-local-authority-test-one")).willThrow(NotFoundException.class);


        ResultActions resultActions =
                mockMvc.perform(get("/api/admin/local-authority/uuid-local-authority-test-one/dcId"))
                    .andExpect(status().isNotFound())
                    .andDo(print());
    }
}