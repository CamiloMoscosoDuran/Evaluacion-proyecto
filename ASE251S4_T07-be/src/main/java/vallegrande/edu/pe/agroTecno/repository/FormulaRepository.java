package vallegrande.edu.pe.agroTecno.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import vallegrande.edu.pe.agroTecno.model.Formula;

public interface FormulaRepository extends ReactiveMongoRepository<Formula, String> {

    Flux<Formula> findByStatus(Boolean status);

}