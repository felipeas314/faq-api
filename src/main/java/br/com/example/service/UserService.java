package br.com.example.service;

import br.com.example.exception.BusinessException;
import br.com.example.model.User;
import br.com.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Mono<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Mono<User> create(User user) {
        return userRepository.existsByUsername(user.getUsername())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new BusinessException("Username já existe", HttpStatus.CONFLICT));
                    }
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                    return userRepository.save(user);
                });
    }

    public Mono<Boolean> checkPassword(User user, String rawPassword) {
        return Mono.just(passwordEncoder.matches(rawPassword, user.getPassword()));
    }
}
