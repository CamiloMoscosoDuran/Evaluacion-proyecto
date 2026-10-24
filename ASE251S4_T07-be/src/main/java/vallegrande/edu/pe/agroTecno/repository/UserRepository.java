package vallegrande.edu.pe.agroTecno.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.agroTecno.model.User;

public interface UserRepository extends ReactiveCrudRepository<User, Integer> {

    @Query("SELECT * FROM seguridad.[user] WHERE user_id = :id")
    Mono<User> findById(Integer id);

    @Query("SELECT * FROM seguridad.[user] WHERE username = :username")
    Mono<User> findByUsername(String username);

    @Query("SELECT * FROM seguridad.[user] WHERE status = :status")
    Flux<User> findByStatus(Boolean status);

    @Query("SELECT * FROM seguridad.[user] WHERE role = :role")
    Flux<User> findByRole(String role);
}
