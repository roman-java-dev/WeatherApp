package com.example.weatherapp.service.impl;

import com.example.weatherapp.dto.UserRegisterDto;
import com.example.weatherapp.model.User;
import com.example.weatherapp.service.AuthenticationService;
import com.example.weatherapp.service.SenderService;
import com.example.weatherapp.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private static final int CODE_LENGTH = 12;
    private final PasswordEncoder passwordEncoder;
    private final TemplateEngine templateEngine;
    private static final Random random = new Random();
    private final UserService userService;
    private final ObjectMapper mapper;
    private final SenderService senderService;
    @Value("${api.subject.message}")
    private String subjectMessage;
    @Value("${api.verification.url}")
    private String verificationUrl;

    public User register(UserRegisterDto registerDto) {
        User user = mapper.convertValue(registerDto, User.class);
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setAccountVerified(false);
        user.setVerificationCode(passwordEncoder.encode(getRandomCode()));
        sendMessage(user);
        return userService.add(user);
    }

    @Override
    public void login(String username, String password) {
        Optional<User> user = userService.getByEmail(username);
        if (user.isEmpty()|| !passwordEncoder.matches(password, user.get().getPassword())) {
            throw new AuthenticationException("Invalid email or password") {
                @Override
                public String getMessage() {
                    return super.getMessage();
                }
            };
        }
        if (!user.get().isAccountVerified()) {
            throw new AuthenticationException("User is not verified") {
                @Override
                public String getMessage() {
                    return super.getMessage();
                }
            };
        }
    }

    @Override
    public void verification(String code) {
        Optional<User> userOptional = userService.findByCode(code);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setAccountVerified(true);
            userService.add(user);
        }
    }

    private void sendMessage(User user) {
        Context context = new Context();
        context.setVariable("user", user);
        context.setVariable("verificationUrl", String.format(verificationUrl, user.getVerificationCode()));
        String emailContent = templateEngine.process("email_template", context);

        senderService.sendMessage(
                user.getEmail(),
                subjectMessage,
                emailContent
        );
    }

    private String getRandomCode() {
        StringBuilder codeBuilder = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            int digit = random.nextInt(10);
            codeBuilder.append(digit);
        }
        return codeBuilder.toString();
    }
}
