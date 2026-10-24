package vallegrande.edu.pe.agroTecno.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.agroTecno.model.PaymentCollection;

public interface PaymentCollectionService {
    Flux<PaymentCollection> findAll();
    Flux<PaymentCollection> findByOrderId(Integer orderId);
    Flux<PaymentCollection> findByPaymentStatus(String paymentStatus);
    Mono<PaymentCollection> findById(Integer id);
    Mono<PaymentCollection> save(PaymentCollection paymentCollection);
    Mono<PaymentCollection> update(Integer id, PaymentCollection paymentCollection);
}
