package com.example.weatherapp;

import com.example.weatherapp.dto.InputDataDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
public class WeatherDetailsIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "romankinash09@gmail.com", password = "12345678", authorities = "USER")
    public void testInputFormEndpoint() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/weather"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("input"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("inputDataDto"));
    }

    @Test
    @WithMockUser(username = "romankinash09@gmail.com", password = "12345678", authorities = "USER")
    public void testProcessFormEndpoint() throws Exception {
        InputDataDto inputDataDto = new InputDataDto();
        inputDataDto.setCity("Lviv");

        mockMvc.perform(post("/processForm")
                        .flashAttr("inputDataDto", inputDataDto))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("response_short"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("weather"));
    }

    @Test
    @WithMockUser(username = "romankinash09@gmail.com", password = "12345678", authorities = "USER")
    public void testSpecializedFormEndpoint() throws Exception {
        mockMvc.perform(post("/specializedForm"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("response"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("weather"));
    }
}
