package org.example.auth;

import lombok.RequiredArgsConstructor;
import org.example.user.User;
import org.example.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    public void signup(@RequestBody User user) {
        userRepository.save(
                User.builder()
                        .email(user.getEmail())
                        .nickname(user.getNickname())
                        .password(passwordEncoder.encode(user.getPassword()))
                        .build()
        );
    }

    @PostMapping("/login")
    public String login(@RequestBody User user) {
        User saved = userRepository.findByEmail(user.getEmail())
                .orElseThrow();

        if (!passwordEncoder.matches(user.getPassword(), saved.getPassword())) {
            throw new RuntimeException("비번 틀림");
        }

        return jwtTokenProvider.createToken(saved.getId());
    }
}

