package com.example.weatherapp;

import com.example.weatherapp.controller.AuthenticationController;
import com.example.weatherapp.dto.UserRegisterDto;
import com.example.weatherapp.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
public class AuthenticationControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testLoginForm() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/login"))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.view().name("login"))
               .andExpect(MockMvcResultMatchers.model().attributeExists("userLoginDto"));
    }

    @Test
    public void testRegisterForm() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/register"))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.view().name("register"))
               .andExpect(MockMvcResultMatchers.model().attributeExists("userRegisterDto"));
    }

    @Test
    public void testRegisterProcessForm_Success() throws Exception {
        UserRegisterDto userRegisterDto = new UserRegisterDto();
        userRegisterDto.setEmail("test@example.com");
        userRegisterDto.setFirstName("First");
        userRegisterDto.setLastName("Last");
        userRegisterDto.setPassword("12345678");

        mockMvc.perform(MockMvcRequestBuilders.post("http://localhost:8091/register")
               .flashAttr("userRegisterDto", userRegisterDto))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.view().name("verification"));
    }

    @Test
    public void testRegisterProcessForm_Failure() throws Exception {
        UserRegisterDto userRegisterDto = new UserRegisterDto();
        userRegisterDto.setEmail("invalidEmail");

        mockMvc.perform(MockMvcRequestBuilders.post("http://localhost:8091/register")
               .flashAttr("userRegisterDto", userRegisterDto))
                .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.view().name("register"));
    }

    @Test
    public void testVerifiedUser() throws Exception {
        String verificationCode = "$2a$10$BuvSCU33rUpYRhNRhCKwjOnAEGy62ATom8ouatQB0/acg9F1xlSny";
        mockMvc.perform(MockMvcRequestBuilders.get("http://localhost:8091/verified/"+verificationCode))
//               .param("code", verificationCode))
               .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
               .andExpect(MockMvcResultMatchers.redirectedUrl("http://localhost:8091/login"));
    }
}
