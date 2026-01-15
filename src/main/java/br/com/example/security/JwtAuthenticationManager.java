package br.com.example.security;

import br.com.example.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtService jwtService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = authentication.getCredentials().toString();

        if (!jwtService.validateToken(token)) {
            return Mono.empty();
        }

        String username = jwtService.getUsernameFromToken(token);
        List<String> roles = jwtService.getRolesFromToken(token);

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(Role::valueOf)
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .toList();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                username,
                null,
                authorities
        );

        return Mono.just(auth);
    }
}
