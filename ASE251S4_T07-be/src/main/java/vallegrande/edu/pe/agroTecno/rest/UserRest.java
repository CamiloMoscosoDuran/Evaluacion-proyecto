package vallegrande.edu.pe.agroTecno.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.agroTecno.model.User;
import vallegrande.edu.pe.agroTecno.service.UserService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/v1/api/users")
public class UserRest {

    private final UserService userService;

    public UserRest(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Flux<User> findAll() {
        return userService.findAll();
    }

    @GetMapping("/status/{status}")
    public Flux<User> findByStatus(@PathVariable Boolean status) {
        return userService.findByStatus(status);
    }

    @GetMapping("/role/{role}")
    public Flux<User> findByRole(@PathVariable String role) {
        return userService.findByRole(role);
    }

    @GetMapping("/{id}")
    public Mono<User> findById(@PathVariable Integer id) {
        return userService.findById(id);
    }

    @GetMapping("/username/{username}")
    public Mono<User> findByUsername(@PathVariable String username) {
        return userService.findByUsername(username);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<User> save(@RequestBody User user) {
        return userService.save(user);
    }

}
