package vallegrande.edu.pe.agroTecno.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.agroTecno.model.Installment;

public interface InstallmentService {
    Flux<Installment> findAll();
    Flux<Installment> findByCollectionId(Integer collectionId);
    Flux<Installment> findByStatus(String status);
    Mono<Installment> findById(Integer id);
    Mono<Installment> save(Integer collectionId, Installment installment);
    Mono<Installment> update(Integer id, Installment installment);
    Mono<Installment> delete(Integer id);
    Mono<Installment> restore(Integer id);
}
