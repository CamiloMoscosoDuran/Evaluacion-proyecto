package vallegrande.edu.pe.agroTecno.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import vallegrande.edu.pe.agroTecno.dto.CollectionDetailDto;
import vallegrande.edu.pe.agroTecno.model.CollectionDetail;
import vallegrande.edu.pe.agroTecno.model.PaymentCollection;
import vallegrande.edu.pe.agroTecno.repository.CollectionDetailRepository;
import vallegrande.edu.pe.agroTecno.repository.PaymentCollectionRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollectionDetailServiceImplTest {

    @Mock
    private CollectionDetailRepository collectionDetailRepository;

    @Mock
    private PaymentCollectionRepository paymentCollectionRepository;

    @InjectMocks
    private CollectionDetailServiceImpl collectionDetailService;

    @Test
    void createUsesExistingCollectionAndSetsDefaults() {
        PaymentCollection paymentCollection = new PaymentCollection();
        paymentCollection.setCollectionId(12);
        CollectionDetailDto request = new CollectionDetailDto();
        request.setCollectionId(999);
        request.setLineNumber(1);
        request.setDescription("  Pago inicial  ");
        request.setStatus(false);

        when(paymentCollectionRepository.findById(12)).thenReturn(Mono.just(paymentCollection));
        when(collectionDetailRepository.save(any(CollectionDetail.class))).thenAnswer(invocation -> {
            CollectionDetail saved = invocation.getArgument(0);
            saved.setDetailId(44);
            return Mono.just(saved);
        });

        StepVerifier.create(collectionDetailService.create(12, request))
                .assertNext(saved -> {
                    assertEquals(44, saved.getDetailId());
                    assertEquals(12, saved.getCollectionId());
                    assertEquals(1, saved.getLineNumber());
                    assertEquals("Pago inicial", saved.getDescription());
                    assertTrue(saved.getStatus());
                    assertTrue(saved.getCreatedAt() != null);
                })
                .verifyComplete();
    }

    @Test
    void readsDetailsByCollectionAndById() {
        PaymentCollection paymentCollection = new PaymentCollection();
        paymentCollection.setCollectionId(12);
        CollectionDetail detail = new CollectionDetail();
        detail.setDetailId(44);
        detail.setCollectionId(12);
        detail.setLineNumber(1);
        detail.setDescription("Pago inicial");
        detail.setStatus(true);

        when(paymentCollectionRepository.findById(12)).thenReturn(Mono.just(paymentCollection));
        when(collectionDetailRepository.findByCollectionId(12)).thenReturn(Flux.just(detail));
        when(collectionDetailRepository.findById(44)).thenReturn(Mono.just(detail));

        StepVerifier.create(collectionDetailService.findByCollectionId(12))
                .assertNext(found -> assertEquals(44, found.getDetailId()))
                .verifyComplete();

        StepVerifier.create(collectionDetailService.findById(12, 44))
                .assertNext(found -> {
                    assertEquals(12, found.getCollectionId());
                    assertEquals("Pago inicial", found.getDescription());
                })
                .verifyComplete();
    }

    @Test
    void createRejectsUnknownPaymentCollectionWithoutSavingDetail() {
        CollectionDetailDto request = new CollectionDetailDto();
        request.setLineNumber(1);
        request.setDescription("Pago inicial");
        when(paymentCollectionRepository.findById(404)).thenReturn(Mono.empty());

        StepVerifier.create(collectionDetailService.create(404, request))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException
                        && error.getMessage().contains("404"))
                .verify();

        verify(collectionDetailRepository, never()).save(any(CollectionDetail.class));
    }

    @Test
    void deleteMarksDetailInactiveAndSetsDeletedAt() {
        CollectionDetail detail = new CollectionDetail();
        detail.setDetailId(44);
        detail.setCollectionId(12);
        detail.setStatus(true);
        LocalDateTime previousUpdatedAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        detail.setUpdatedAt(previousUpdatedAt);
        PaymentCollection paymentCollection = new PaymentCollection();
        paymentCollection.setCollectionId(12);
        when(paymentCollectionRepository.findById(12)).thenReturn(Mono.just(paymentCollection));
        when(collectionDetailRepository.findById(44)).thenReturn(Mono.just(detail));
        when(collectionDetailRepository.save(any(CollectionDetail.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(collectionDetailService.delete(12, 44))
                .assertNext(deleted -> {
                    assertEquals(12, deleted.getCollectionId());
                    assertTrue(Boolean.FALSE.equals(deleted.getStatus()));
                    assertTrue(deleted.getDeletedAt() != null);
                    assertEquals(previousUpdatedAt, deleted.getUpdatedAt());
                })
                .verifyComplete();
    }

    @Test
    void updateChangesEditableFieldsAndSetsUpdatedAt() {
        CollectionDetail detail = new CollectionDetail();
        detail.setDetailId(44);
        detail.setCollectionId(12);
        detail.setLineNumber(1);
        detail.setDescription("Descripción original");
        detail.setStatus(true);
        PaymentCollection paymentCollection = new PaymentCollection();
        paymentCollection.setCollectionId(12);
        CollectionDetailDto request = new CollectionDetailDto();
        request.setLineNumber(2);
        request.setDescription("  Descripción corregida  ");
        request.setStatus(false);

        when(paymentCollectionRepository.findById(12)).thenReturn(Mono.just(paymentCollection));
        when(collectionDetailRepository.findById(44)).thenReturn(Mono.just(detail));
        when(collectionDetailRepository.save(any(CollectionDetail.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(collectionDetailService.update(12, 44, request))
                .assertNext(updated -> {
                    assertEquals(2, updated.getLineNumber());
                    assertEquals("Descripción corregida", updated.getDescription());
                    assertTrue(updated.getStatus());
                    assertTrue(updated.getUpdatedAt() != null);
                })
                .verifyComplete();
    }

    @Test
    void restoreSetsRestoredAtAndPreservesDeletedAndUpdatedDates() {
        LocalDateTime deletedAt = LocalDateTime.of(2026, 1, 1, 9, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        CollectionDetail detail = new CollectionDetail();
        detail.setDetailId(44);
        detail.setCollectionId(12);
        detail.setStatus(false);
        detail.setDeletedAt(deletedAt);
        detail.setUpdatedAt(updatedAt);
        PaymentCollection paymentCollection = new PaymentCollection();
        paymentCollection.setCollectionId(12);

        when(paymentCollectionRepository.findById(12)).thenReturn(Mono.just(paymentCollection));
        when(collectionDetailRepository.findById(44)).thenReturn(Mono.just(detail));
        when(collectionDetailRepository.save(any(CollectionDetail.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(collectionDetailService.restore(12, 44))
                .assertNext(restored -> {
                    assertTrue(restored.getStatus());
                    assertTrue(restored.getRestoredAt() != null);
                    assertEquals(deletedAt, restored.getDeletedAt());
                    assertEquals(updatedAt, restored.getUpdatedAt());
                })
                .verifyComplete();
    }
}