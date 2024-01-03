package com.example.weatherapp.service.impl;

import com.example.weatherapp.dto.UserRegisterDto;
import com.example.weatherapp.model.User;
import com.example.weatherapp.service.SenderService;
import com.example.weatherapp.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceImplTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserService userService;

    @Mock
    private SenderService senderService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(authenticationService, "mapper", new ObjectMapper());
        ReflectionTestUtils.setField(authenticationService, "verificationUrl", "http://localhost:8091/verified?code=%s");
        ReflectionTestUtils.setField(authenticationService,"templateEngine", templateEngine());
        ReflectionTestUtils.setField(authenticationService, "subjectMessage", "Registration confirmation");
    }

    @Test
    void testRegister() {
        UserRegisterDto registerDto = new UserRegisterDto();
        registerDto.setFirstName("Test");
        registerDto.setLastName("Test-Test");
        registerDto.setEmail("test@example.com");
        registerDto.setPassword("password");

        User user = new User();
        user.setFirstName(registerDto.getFirstName());
        user.setLastName(registerDto.getLastName());
        user.setEmail(registerDto.getEmail());
        user.setPassword(registerDto.getPassword());
        user.setAccountVerified(false);

        Context context = new Context();
        context.setVariable("user", user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userService.add(any(User.class))).thenReturn(user);


        User registeredUser = authenticationService.register(registerDto);

        assertNotNull(registeredUser);
        assertEquals(registerDto.getEmail(), registeredUser.getEmail());
        assertFalse(registeredUser.isAccountVerified());
        verify(passwordEncoder, times(1)).encode(eq(registerDto.getPassword()));
        verify(userService, times(1)).add(any(User.class));
        verify(senderService, times(1)).sendMessage(anyString(), anyString(), anyString());
    }

    @Test
    void testLogin() {
        String username = "test@example.com";
        String password = "testPassword";
        User user = new User();
        user.setEmail(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setAccountVerified(true);

        when(userService.getByEmail(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);

        authenticationService.login(username, password);
    }

    @Test
    void login_ValidCredentialsAndVerifiedUser() {
        String username = "test@example.com";
        String password = "password";
        User user = new User();
        user.setEmail(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setAccountVerified(true);

        when(userService.getByEmail(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);

        assertDoesNotThrow(() -> authenticationService.login(username, password));
    }

    @Test
    void login_InvalidPassword() {
        String username = "test@example.com";
        String password = "wrongPassword";
        User user = new User();
        user.setEmail(username);
        user.setPassword(passwordEncoder.encode("correctPassword")); // Correct password is different

        when(userService.getByEmail(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(false);

        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> authenticationService.login(username, password));

        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void login_UserNotVerified() {
        String username = "test@example.com";
        String password = "password";
        User user = new User();
        user.setEmail(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setAccountVerified(false);

        when(userService.getByEmail(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);

        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> authenticationService.login(username, password));

        assertEquals("User is not verified", exception.getMessage());
    }

    @Test
    void login_UserNotFound() {
        String username = "nonexistent@example.com";

        when(userService.getByEmail(username)).thenReturn(Optional.empty());

        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> authenticationService.login(username, "password"));

        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void testLoginWithInvalidCredentials() {
        String username = "test@example.com";
        String password = "testPassword";
        when(userService.getByEmail(username)).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class, () -> authenticationService.login(username, password));
    }

    @Test
    void testLoginWithUnverifiedAccount() {
        String username = "test@example.com";
        String password = "testPassword";
        User user = new User();
        user.setEmail(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setAccountVerified(false);

        when(userService.getByEmail(username)).thenReturn(Optional.of(user));

        assertThrows(AuthenticationException.class, () -> authenticationService.login(username, password));
    }

    @Test
    void testVerification() {
        String verificationCode = "123456789012";
        User user = new User();
        user.setVerificationCode(verificationCode);
        when(userService.findByCode(verificationCode)).thenReturn(Optional.of(user));

        authenticationService.verification(verificationCode);

        assertTrue(user.isAccountVerified());
        verify(userService, times(1)).add(user);
    }

    private ClassLoaderTemplateResolver templateResolver() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML");
        templateResolver.setCharacterEncoding("UTF-8");
        return templateResolver;
    }

    private SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        return templateEngine;
    }
}
