package br.com.example.service;

import br.com.example.dto.request.LoginRequest;
import br.com.example.dto.request.RegisterRequest;
import br.com.example.dto.response.AuthResponse;
import br.com.example.exception.UnauthorizedException;
import br.com.example.model.User;
import br.com.example.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;

    public Mono<AuthResponse> login(LoginRequest request) {
        return userService.findByUsername(request.getUsername())
                .flatMap(user -> userService.checkPassword(user, request.getPassword())
                        .flatMap(matches -> {
                            if (!matches) {
                                return Mono.error(new UnauthorizedException("Credenciais inválidas"));
                            }
                            String token = jwtService.generateToken(user);
                            return Mono.just(AuthResponse.builder()
                                    .token(token)
                                    .username(user.getUsername())
                                    .build());
                        }))
                .switchIfEmpty(Mono.error(new UnauthorizedException("Credenciais inválidas")));
    }

    public Mono<AuthResponse> register(RegisterRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .roles(List.of(request.getRole()))
                .enabled(true)
                .build();

        return userService.create(user)
                .map(savedUser -> {
                    String token = jwtService.generateToken(savedUser);
                    return AuthResponse.builder()
                            .token(token)
                            .username(savedUser.getUsername())
                            .build();
                });
    }
}
