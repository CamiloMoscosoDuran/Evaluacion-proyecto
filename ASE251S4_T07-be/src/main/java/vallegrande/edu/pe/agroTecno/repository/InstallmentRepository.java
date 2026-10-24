package vallegrande.edu.pe.agroTecno.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.agroTecno.model.Installment;

public interface InstallmentRepository extends ReactiveCrudRepository<Installment, Integer> {

    @Query("SELECT * FROM cobranzas.installment WHERE installment_id = :id")
    Mono<Installment> findById(Integer id);

    @Query("SELECT * FROM cobranzas.installment WHERE collection_id = :collectionId")
    Flux<Installment> findByCollectionId(Integer collectionId);

    @Query("SELECT * FROM cobranzas.installment WHERE status = :status")
    Flux<Installment> findByStatus(String status);

    @Query("SELECT * FROM cobranzas.installment WHERE collection_id = :collectionId AND status = :status")
    Flux<Installment> findByCollectionIdAndStatus(Integer collectionId, String status);
}
