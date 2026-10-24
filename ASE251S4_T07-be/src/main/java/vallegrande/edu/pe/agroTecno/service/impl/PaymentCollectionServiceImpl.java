package vallegrande.edu.pe.agroTecno.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.agroTecno.model.PaymentCollection;
import vallegrande.edu.pe.agroTecno.repository.CustomerRepository;
import vallegrande.edu.pe.agroTecno.repository.PaymentCollectionRepository;
import vallegrande.edu.pe.agroTecno.repository.SalesOrderRepository;
import vallegrande.edu.pe.agroTecno.service.PaymentCollectionService;

@Service
public class PaymentCollectionServiceImpl implements PaymentCollectionService {

    private static final Set<String> PAYMENT_STATUSES = Set.of("P", "C", "A");

    private final PaymentCollectionRepository paymentCollectionRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final CustomerRepository customerRepository;

    public PaymentCollectionServiceImpl(PaymentCollectionRepository paymentCollectionRepository,
                                        SalesOrderRepository salesOrderRepository,
                                        CustomerRepository customerRepository) {
        this.paymentCollectionRepository = paymentCollectionRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public Flux<PaymentCollection> findAll() { return paymentCollectionRepository.findAll(); }

    @Override
    public Flux<PaymentCollection> findByOrderId(Integer orderId) { return paymentCollectionRepository.findByOrderId(orderId); }

    @Override
    public Flux<PaymentCollection> findByPaymentStatus(String paymentStatus) {
        validatePaymentStatus(paymentStatus);
        return paymentCollectionRepository.findByPaymentStatus(paymentStatus);
    }

    @Override
    public Mono<PaymentCollection> findById(Integer id) { return paymentCollectionRepository.findById(id); }

    @Override
    public Mono<PaymentCollection> save(PaymentCollection paymentCollection) {
        validate(paymentCollection);
        return salesOrderRepository.findById(paymentCollection.getOrderId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Sales order not found with ID: " + paymentCollection.getOrderId())))
                .flatMap(order -> customerRepository.findById(order.getMongoCustomerId())
                        .switchIfEmpty(Mono.error(new IllegalArgumentException(
                                "Customer not found in MongoDB with ID: " + order.getMongoCustomerId())))
                        .then(paymentCollectionRepository.existsByOrderId(order.getOrderId()))
                        .flatMap(exists -> {
                            if (exists) {
                                return Mono.error(new IllegalArgumentException(
                                        "Payment collection already exists for sales order: " + order.getOrderId()));
                            }
                            paymentCollection.setMongoCustomerId(order.getMongoCustomerId());
                            paymentCollection.setTotalAmount(order.getTotalOrder());
                            paymentCollection.setPaymentStatus(paymentCollection.getPaymentStatus() == null
                                    ? "P" : paymentCollection.getPaymentStatus());
                            if (paymentCollection.getCreatedAt() == null) {
                                paymentCollection.setCreatedAt(LocalDateTime.now());
                            }
                            return paymentCollectionRepository.save(paymentCollection);
                        }));
    }

    @Override
    public Mono<PaymentCollection> update(Integer id, PaymentCollection paymentCollection) {
        validate(paymentCollection);
        return paymentCollectionRepository.findById(id)
            .switchIfEmpty(Mono.error(new IllegalArgumentException("Payment collection not found with ID: " + id)))
            .flatMap(existing -> salesOrderRepository.findById(paymentCollection.getOrderId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                    "Sales order not found with ID: " + paymentCollection.getOrderId())))
                .flatMap(order -> customerRepository.findById(order.getMongoCustomerId())
                    .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "Customer not found in MongoDB with ID: " + order.getMongoCustomerId())))
                    .then(paymentCollectionRepository.findByOrderId(order.getOrderId())
                        .filter(other -> !other.getCollectionId().equals(existing.getCollectionId()))
                        .hasElements())
                    .flatMap(hasOtherCollection -> {
                        if (hasOtherCollection) {
                        return Mono.error(new IllegalArgumentException(
                            "Payment collection already exists for sales order: " + order.getOrderId()));
                        }
                        existing.setOrderId(order.getOrderId());
                        existing.setMongoCustomerId(order.getMongoCustomerId());
                        existing.setTotalAmount(order.getTotalOrder());
                        existing.setInstallmentsCount(paymentCollection.getInstallmentsCount());
                        existing.setPaymentStatus(paymentCollection.getPaymentStatus());
                        existing.setUpdatedAt(LocalDateTime.now());
                        return paymentCollectionRepository.save(existing);
                    })));
    }

    private void validate(PaymentCollection paymentCollection) {
        if (paymentCollection == null || paymentCollection.getOrderId() == null
            || paymentCollection.getInstallmentsCount() == null
                || paymentCollection.getInstallmentsCount() <= 0) {
            throw new IllegalArgumentException("order_id and installments_count are required and must be valid");
        }
        if (paymentCollection.getPaymentStatus() != null) {
            validatePaymentStatus(paymentCollection.getPaymentStatus());
        }
    }

    private void validatePaymentStatus(String paymentStatus) {
        if (!PAYMENT_STATUSES.contains(paymentStatus)) {
            throw new IllegalArgumentException("payment_status must be P, C or A");
        }
    }
}
