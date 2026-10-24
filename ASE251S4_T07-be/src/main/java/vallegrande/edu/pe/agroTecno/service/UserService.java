package vallegrande.edu.pe.agroTecno.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.agroTecno.model.User;

public interface UserService {
    Flux<User> findAll();
    Flux<User> findByStatus(Boolean status);
    Flux<User> findByRole(String role);
    Mono<User> findById(Integer id);
    Mono<User> findByUsername(String username);
    Mono<User> save(User user);
}
