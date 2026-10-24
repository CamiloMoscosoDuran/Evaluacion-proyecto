package vallegrande.edu.pe.agroTecno.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.agroTecno.model.Installment;
import vallegrande.edu.pe.agroTecno.repository.InstallmentRepository;
import vallegrande.edu.pe.agroTecno.repository.PaymentCollectionRepository;
import vallegrande.edu.pe.agroTecno.service.InstallmentService;

@Service
public class InstallmentServiceImpl implements InstallmentService {

    private static final Set<String> PAYMENT_STATUSES = Set.of("P", "C", "A");

    private final InstallmentRepository installmentRepository;
    private final PaymentCollectionRepository paymentCollectionRepository;

    public InstallmentServiceImpl(InstallmentRepository installmentRepository,
                                 PaymentCollectionRepository paymentCollectionRepository) {
        this.installmentRepository = installmentRepository;
        this.paymentCollectionRepository = paymentCollectionRepository;
    }

    @Override
    public Flux<Installment> findAll() {
        return installmentRepository.findAll();
    }

    @Override
    public Flux<Installment> findByCollectionId(Integer collectionId) {
        return installmentRepository.findByCollectionId(collectionId);
    }

    @Override
    public Flux<Installment> findByStatus(String status) {
        validateStatus(status);
        return installmentRepository.findByStatus(status);
    }

    @Override
    public Mono<Installment> findById(Integer id) {
        return installmentRepository.findById(id);
    }

    @Override
    public Mono<Installment> save(Integer collectionId, Installment installment) {
        validateCollectionId(collectionId);
        validateInstallment(installment);
        return paymentCollectionRepository.findById(collectionId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Payment collection not found with ID: " + collectionId)))
                .then(Mono.defer(() -> {
                    installment.setCollectionId(collectionId);
                    if (installment.getAmountPaid() == null) {
                        installment.setAmountPaid(BigDecimal.ZERO);
                    }
                    installment.setStatus("P");
                    installment.setCreatedAt(LocalDateTime.now());
                    return installmentRepository.save(installment);
                }));
    }

    @Override
    public Mono<Installment> update(Integer id, Installment installment) {
        validateInstallment(installment);
        return installmentRepository.findById(id)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Installment not found with ID: " + id)))
                        .flatMap(existing -> {
                            existing.setInstallmentNum(installment.getInstallmentNum());
                            existing.setDueDate(installment.getDueDate());
                            existing.setAmountDue(installment.getAmountDue());
                            existing.setAmountPaid(installment.getAmountPaid());
                            existing.setPaymentType(installment.getPaymentType());
                            existing.setPaymentDate(installment.getPaymentDate());
                            existing.setObservation(installment.getObservation());
                            if (!"C".equals(existing.getStatus())) {
                                existing.setStatus(isFullyPaid(existing) ? "A" : "P");
                            }
                            existing.setUpdatedAt(LocalDateTime.now());
                            return installmentRepository.save(existing);
                        });
    }

    @Override
    public Mono<Installment> delete(Integer id) {
        return installmentRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Installment not found with ID: " + id)))
                .flatMap(existing -> {
                    existing.setStatus("C");
                    existing.setDeletedAt(LocalDateTime.now());
                    return installmentRepository.save(existing);
                });
    }

    @Override
    public Mono<Installment> restore(Integer id) {
        return installmentRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Installment not found with ID: " + id)))
                .flatMap(existing -> {
                    if (existing.getDeletedAt() == null) {
                        return Mono.error(new IllegalStateException("Installment is not logically deleted"));
                    }
                            existing.setStatus(isFullyPaid(existing) ? "A" : "P");
                    existing.setRestoredAt(LocalDateTime.now());
                    return installmentRepository.save(existing);
                });
    }

    private void validateCollectionId(Integer collectionId) {
        if (collectionId == null || collectionId <= 0) {
            throw new IllegalArgumentException("collectionId must be a valid positive number");
        }
    }

    private boolean isFullyPaid(Installment installment) {
        return installment.getAmountDue() != null
                && installment.getAmountPaid() != null
                && installment.getAmountDue().compareTo(installment.getAmountPaid()) == 0;
    }

    private void validateInstallment(Installment installment) {
        if (installment == null) {
            throw new IllegalArgumentException("Installment request is required");
        }
        if (installment.getInstallmentNum() == null || installment.getInstallmentNum() <= 0) {
            throw new IllegalArgumentException("installment_num is required and must be greater than 0");
        }
        if (installment.getDueDate() == null) {
            throw new IllegalArgumentException("due_date is required");
        }
        if (installment.getAmountDue() == null || installment.getAmountDue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount_due is required and must be greater than 0");
        }
        if (installment.getAmountPaid() != null && installment.getAmountPaid().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("amount_paid cannot be negative");
        }
    }

    private void validateStatus(String status) {
        if (!PAYMENT_STATUSES.contains(status)) {
            throw new IllegalArgumentException("status must be P, C or A");
        }
    }
}
