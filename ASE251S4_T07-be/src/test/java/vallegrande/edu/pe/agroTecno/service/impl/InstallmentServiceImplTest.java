package vallegrande.edu.pe.agroTecno.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import vallegrande.edu.pe.agroTecno.model.Installment;
import vallegrande.edu.pe.agroTecno.model.PaymentCollection;
import vallegrande.edu.pe.agroTecno.repository.InstallmentRepository;
import vallegrande.edu.pe.agroTecno.repository.PaymentCollectionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstallmentServiceImplTest {

    @Mock
    private InstallmentRepository installmentRepository;

    @Mock
    private PaymentCollectionRepository paymentCollectionRepository;

    @InjectMocks
    private InstallmentServiceImpl installmentService;

    @Test
    void createValidatesCollectionAndForcesPendingStatusAndCreationDate() {
        PaymentCollection collection = new PaymentCollection();
        collection.setCollectionId(12);
        Installment request = validInstallment();
        request.setStatus("C");
        request.setCollectionId(999);
        request.setCreatedAt(LocalDateTime.of(2020, 1, 1, 0, 0));

        when(paymentCollectionRepository.findById(12)).thenReturn(Mono.just(collection));
        when(installmentRepository.save(any(Installment.class))).thenAnswer(invocation -> {
            Installment saved = invocation.getArgument(0);
            saved.setInstallmentId(77);
            return Mono.just(saved);
        });

        StepVerifier.create(installmentService.save(12, request))
                .assertNext(saved -> {
                    assertEquals(77, saved.getInstallmentId());
                    assertEquals(12, saved.getCollectionId());
                    assertEquals("P", saved.getStatus());
                    assertNotNull(saved.getCreatedAt());
                    assertNotEquals(LocalDateTime.of(2020, 1, 1, 0, 0), saved.getCreatedAt());
                })
                .verifyComplete();
    }

            @Test
            void updateMarksInstallmentPaidWhenAmountsMatch() {
            Installment existing = validInstallment();
            existing.setInstallmentId(79);
            existing.setStatus("P");
            Installment request = validInstallment();
            request.setAmountDue(new BigDecimal("110.00"));
            request.setAmountPaid(new BigDecimal("110.00"));
            when(installmentRepository.findById(79)).thenReturn(Mono.just(existing));
            when(installmentRepository.save(any(Installment.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

            StepVerifier.create(installmentService.update(79, request))
                .assertNext(updated -> assertEquals("A", updated.getStatus()))
                .verifyComplete();
            }

    @Test
    void createRejectsUnknownCollectionWithoutSaving() {
        Installment request = validInstallment();
        when(paymentCollectionRepository.findById(12)).thenReturn(Mono.empty());

        StepVerifier.create(installmentService.save(12, request))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException
                        && error.getMessage().contains("12"))
                .verify();

        verify(installmentRepository, never()).save(any(Installment.class));
    }

    @Test
    void updateSetsUpdatedAtAndDoesNotAcceptStatusFromRequest() {
        Installment existing = validInstallment();
        existing.setInstallmentId(77);
        existing.setStatus("P");
        Installment request = validInstallment();
        request.setStatus("C");
        request.setObservation("Updated note");

        when(installmentRepository.findById(77)).thenReturn(Mono.just(existing));
        when(installmentRepository.save(any(Installment.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(installmentService.update(77, request))
                .assertNext(updated -> {
                    assertEquals("P", updated.getStatus());
                    assertEquals("Updated note", updated.getObservation());
                    assertNotNull(updated.getUpdatedAt());
                })
                .verifyComplete();
    }

    @Test
    void restoreSetsStatusAToPreviouslyFullyPaidInstallment() {
        Installment existing = validInstallment();
        existing.setInstallmentId(78);
        existing.setStatus("C");
        existing.setAmountDue(new BigDecimal("110.00"));
        existing.setAmountPaid(new BigDecimal("110.00"));
        existing.setDeletedAt(LocalDateTime.of(2026, 1, 1, 9, 0));
        when(installmentRepository.findById(78)).thenReturn(Mono.just(existing));
        when(installmentRepository.save(any(Installment.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(installmentService.restore(78))
                .assertNext(restored -> {
                    assertEquals("A", restored.getStatus());
                    assertNotNull(restored.getRestoredAt());
                })
                .verifyComplete();
    }

    @Test
    void deleteSetsStatusCAndDeletedAtWithoutChangingUpdatedAt() {
        Installment existing = validInstallment();
        existing.setInstallmentId(77);
        existing.setStatus("P");
        LocalDateTime previousUpdatedAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        existing.setUpdatedAt(previousUpdatedAt);
        when(installmentRepository.findById(77)).thenReturn(Mono.just(existing));
        when(installmentRepository.save(any(Installment.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(installmentService.delete(77))
                .assertNext(deleted -> {
                    assertEquals("C", deleted.getStatus());
                    assertNotNull(deleted.getDeletedAt());
                    assertEquals(previousUpdatedAt, deleted.getUpdatedAt());
                })
                .verifyComplete();
    }

    @Test
    void restoreSetsStatusPAndRestoredAtWithoutChangingOtherAuditDates() {
        Installment existing = validInstallment();
        existing.setInstallmentId(77);
        existing.setStatus("C");
        LocalDateTime deletedAt = LocalDateTime.of(2026, 1, 1, 9, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        existing.setDeletedAt(deletedAt);
        existing.setUpdatedAt(updatedAt);
        when(installmentRepository.findById(77)).thenReturn(Mono.just(existing));
        when(installmentRepository.save(any(Installment.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(installmentService.restore(77))
                .assertNext(restored -> {
                    assertEquals("P", restored.getStatus());
                    assertNotNull(restored.getRestoredAt());
                    assertEquals(deletedAt, restored.getDeletedAt());
                    assertEquals(updatedAt, restored.getUpdatedAt());
                })
                .verifyComplete();
    }

    private Installment validInstallment() {
        Installment installment = new Installment();
        installment.setCollectionId(12);
        installment.setInstallmentNum(1);
        installment.setDueDate(LocalDate.of(2026, 10, 15));
        installment.setAmountDue(new BigDecimal("100.00"));
        return installment;
    }
}
