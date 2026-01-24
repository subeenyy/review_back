package org.example.auth;

import lombok.RequiredArgsConstructor;
import org.example.user.User;
import org.example.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
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
        private final RefreshTokenRepository refreshTokenRepository;
        private final PasswordEncoder passwordEncoder;

        @PostMapping("/signup")
        public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
                // Check for duplicate email
                if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                        return ResponseEntity.badRequest().body("이미 사용 중인 이메일입니다");
                }

                User user = User.builder()
                                .email(request.getEmail())
                                .nickname(request.getNickname())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .loginType(User.LoginType.REGULAR)
                                .build();

                userRepository.save(user);
                return ResponseEntity.ok().build();
        }

        @PostMapping("/kakao")
        public ResponseEntity<LoginResponse> kakaoLogin(@RequestBody KakaoLoginRequest request) {
                // TODO: Implement Kakao API verification
                // For now, this is a placeholder that expects kakaoUserId in the accessToken
                // field
                String kakaoUserId = request.getAccessToken(); // Placeholder

                // Check if user exists by kakaoUserId
                User user = userRepository.findByKakaoUserId(kakaoUserId)
                                .orElseGet(() -> {
                                        // Auto-create new user for Kakao login
                                        User newUser = User.builder()
                                                        .email("kakao_" + kakaoUserId + "@kakao.com") // Placeholder
                                                                                                      // email
                                                        .nickname("카카오사용자" + kakaoUserId.substring(0,
                                                                        Math.min(4, kakaoUserId.length())))
                                                        .password(passwordEncoder.encode("KAKAO_NO_PASSWORD")) // Placeholder
                                                        .kakaoUserId(kakaoUserId)
                                                        .loginType(User.LoginType.KAKAO)
                                                        .build();
                                        return userRepository.save(newUser);
                                });

                // Generate tokens
                String accessToken = jwtTokenProvider.createAccessToken(user.getId());
                String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

                refreshTokenRepository.save(
                                new RefreshToken(user.getId(), refreshToken));

                return ResponseEntity.ok(
                                new LoginResponse(
                                                accessToken,
                                                refreshToken,
                                                System.currentTimeMillis() + 3600000));
        }

        @PostMapping("/login")
        public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
                // 1. 이메일로 사용자 조회, 없으면 UsernameNotFoundException
                User saved = userRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

                // 2. 비밀번호 확인, 틀리면 BadCredentialsException
                if (!passwordEncoder.matches(request.getPassword(), saved.getPassword())) {
                        throw new BadCredentialsException("비밀번호가 틀립니다.");
                }

                String accessToken = jwtTokenProvider.createAccessToken(saved.getId());
                String refreshToken = jwtTokenProvider.createRefreshToken(saved.getId());

                refreshTokenRepository.save(
                                new RefreshToken(saved.getId(), refreshToken));

                return ResponseEntity.ok(
                                new LoginResponse(
                                                accessToken,
                                                refreshToken,
                                                System.currentTimeMillis() + 3600000));

        }

        @PostMapping("/refresh")
        public ResponseEntity<LoginResponse> refresh(
                        @RequestBody RefreshRequest request) {
                Long userId = jwtTokenProvider.getUserId(request.getRefreshToken());

                RefreshToken saved = refreshTokenRepository.findById(userId)
                                .orElseThrow();

                if (!saved.getToken().equals(request.getRefreshToken())) {
                        throw new IllegalStateException("유효하지 않은 토큰");
                }

                String newAccess = jwtTokenProvider.createAccessToken(userId);

                return ResponseEntity.ok(
                                new LoginResponse(
                                                newAccess,
                                                saved.getToken(),
                                                System.currentTimeMillis() + 3600000));
        }

        @GetMapping("/ping")
        public ResponseEntity<String> ping() {
                return ResponseEntity.ok("pong");
        }
}
