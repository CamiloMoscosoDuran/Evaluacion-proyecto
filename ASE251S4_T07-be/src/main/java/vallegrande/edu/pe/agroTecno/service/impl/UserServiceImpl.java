package vallegrande.edu.pe.agroTecno.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.agroTecno.model.User;
import vallegrande.edu.pe.agroTecno.repository.UserRepository;
import vallegrande.edu.pe.agroTecno.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Flux<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Flux<User> findByStatus(Boolean status) {
        return userRepository.findByStatus(status);
    }

    @Override
    public Flux<User> findByRole(String role) {
        return userRepository.findByRole(role);
    }

    @Override
    public Mono<User> findById(Integer id) {
        return userRepository.findById(id);
    }

    @Override
    public Mono<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Mono<User> save(User user) {
        validate(user);
        if (user.getStatus() == null) {
            user.setStatus(true);
        }
        if (user.getCreatedBy() == null || user.getCreatedBy().isBlank()) {
            user.setCreatedBy("system");
        }
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private void validate(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User is required");
        }
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            throw new IllegalArgumentException("username is required");
        }
        if (user.getPasswordHash() == null || user.getPasswordHash().length == 0) {
            throw new IllegalArgumentException("password_hash is required");
        }
        if (user.getRole() == null || user.getRole().isBlank()) {
            throw new IllegalArgumentException("role is required");
        }
        if (user.getCreatedBy() == null || user.getCreatedBy().isBlank()) {
            user.setCreatedBy("system");
        }
    }
}
