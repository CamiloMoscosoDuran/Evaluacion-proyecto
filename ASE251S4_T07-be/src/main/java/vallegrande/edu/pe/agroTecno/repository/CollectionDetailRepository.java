package vallegrande.edu.pe.agroTecno.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.agroTecno.model.CollectionDetail;

public interface CollectionDetailRepository extends ReactiveCrudRepository<CollectionDetail, Integer> {

    @Query("SELECT * FROM cobranzas.collection_detail WHERE detail_id = :id")
    Mono<CollectionDetail> findById(Integer id);

    @Query("SELECT * FROM cobranzas.collection_detail WHERE collection_id = :collectionId")
    Flux<CollectionDetail> findByCollectionId(Integer collectionId);

    @Query("DELETE FROM cobranzas.collection_detail WHERE collection_id = :collectionId")
    Mono<Void> deleteByCollectionId(Integer collectionId);
}
