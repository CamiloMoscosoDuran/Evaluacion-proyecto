package vallegrande.edu.pe.agroTecno.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.agroTecno.model.PaymentCollection;

public interface PaymentCollectionRepository extends ReactiveCrudRepository<PaymentCollection, Integer> {

    @Query("SELECT * FROM cobranzas.payment_collection WHERE collection_id = :id")
    Mono<PaymentCollection> findById(Integer id);

    @Query("SELECT * FROM cobranzas.payment_collection WHERE order_id = :orderId")
    Flux<PaymentCollection> findByOrderId(Integer orderId);

    @Query("SELECT CASE WHEN COUNT(1) > 0 THEN CAST(1 AS BIT) ELSE CAST(0 AS BIT) END FROM cobranzas.payment_collection WHERE order_id = :orderId")
    Mono<Boolean> existsByOrderId(Integer orderId);

    @Query("SELECT * FROM cobranzas.payment_collection WHERE payment_status = :status")
    Flux<PaymentCollection> findByPaymentStatus(String status);
}
