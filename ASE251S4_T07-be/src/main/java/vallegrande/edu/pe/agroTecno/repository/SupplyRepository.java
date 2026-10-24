package vallegrande.edu.pe.agroTecno.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.agroTecno.model.Supply;

public interface SupplyRepository extends ReactiveMongoRepository<Supply, String> {

    // Listar por estado (true = activo, false = inactivo)
    Flux<Supply> findByStatus(Boolean status);

    // Buscar por nombre único (índice uq_supply_name)
    Mono<Supply> findByName(String name);

    // Listar por categoría (índice ix_supply_category, V006)
    Flux<Supply> findByCategory(String category);

}
