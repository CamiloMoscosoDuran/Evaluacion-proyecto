package vallegrande.edu.pe.agroTecno.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.agroTecno.dto.CollectionDetailDto;
import vallegrande.edu.pe.agroTecno.model.CollectionDetail;
import vallegrande.edu.pe.agroTecno.repository.CollectionDetailRepository;
import vallegrande.edu.pe.agroTecno.repository.PaymentCollectionRepository;
import vallegrande.edu.pe.agroTecno.service.CollectionDetailService;

@Service
public class CollectionDetailServiceImpl implements CollectionDetailService {

    private final CollectionDetailRepository collectionDetailRepository;
    private final PaymentCollectionRepository paymentCollectionRepository;

    public CollectionDetailServiceImpl(CollectionDetailRepository collectionDetailRepository,
                                       PaymentCollectionRepository paymentCollectionRepository) {
        this.collectionDetailRepository = collectionDetailRepository;
        this.paymentCollectionRepository = paymentCollectionRepository;
    }

    @Override
    public Flux<CollectionDetailDto> findByCollectionId(Integer collectionId) {
        if (collectionId == null || collectionId <= 0) {
            return Flux.error(new IllegalArgumentException("collectionId must be a valid positive number"));
        }
        return paymentCollectionRepository.findById(collectionId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "Payment collection not found with ID: " + collectionId)))
                .thenMany(collectionDetailRepository.findByCollectionId(collectionId))
                .map(this::toDto);
    }

    @Override
    public Mono<CollectionDetailDto> findById(Integer collectionId, Integer detailId) {
        return findDetail(collectionId, detailId).map(this::toDto);
    }

    @Override
    public Mono<CollectionDetailDto> create(Integer collectionId, CollectionDetailDto request) {
        validate(collectionId, request);
        return paymentCollectionRepository.findById(collectionId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "Payment collection not found with ID: " + collectionId)))
                .flatMap(paymentCollection -> {
                    CollectionDetail detail = new CollectionDetail();
                    detail.setCollectionId(paymentCollection.getCollectionId());
                    detail.setLineNumber(request.getLineNumber());
                    detail.setDescription(request.getDescription().trim());
                    detail.setStatus(true);
                    detail.setCreatedAt(LocalDateTime.now());
                    return collectionDetailRepository.save(detail).map(this::toDto);
                });
    }

    @Override
    public Mono<CollectionDetailDto> delete(Integer collectionId, Integer detailId) {
        return findDetail(collectionId, detailId)
                .flatMap(detail -> {
                    if (Boolean.FALSE.equals(detail.getStatus())) {
                        return Mono.just(toDto(detail));
                    }
                    detail.setStatus(false);
                    detail.setDeletedAt(LocalDateTime.now());
                    return collectionDetailRepository.save(detail).map(this::toDto);
                });
    }

    @Override
    public Mono<CollectionDetailDto> update(Integer collectionId, Integer detailId, CollectionDetailDto request) {
        validate(collectionId, request);
        return findDetail(collectionId, detailId)
                .flatMap(detail -> {
                    detail.setLineNumber(request.getLineNumber());
                    detail.setDescription(request.getDescription().trim());
                    detail.setUpdatedAt(LocalDateTime.now());
                    return collectionDetailRepository.save(detail).map(this::toDto);
                });
    }

    @Override
    public Mono<CollectionDetailDto> restore(Integer collectionId, Integer detailId) {
        return findDetail(collectionId, detailId)
                .flatMap(detail -> {
                    if (Boolean.TRUE.equals(detail.getStatus())) {
                        return Mono.just(toDto(detail));
                    }
                    detail.setStatus(true);
                    detail.setRestoredAt(LocalDateTime.now());
                    return collectionDetailRepository.save(detail).map(this::toDto);
                });
    }

    private Mono<CollectionDetail> findDetail(Integer collectionId, Integer detailId) {
        if (collectionId == null || collectionId <= 0 || detailId == null || detailId <= 0) {
            return Mono.error(new IllegalArgumentException("collectionId and detailId must be valid positive numbers"));
        }
        return paymentCollectionRepository.findById(collectionId)
            .switchIfEmpty(Mono.error(new IllegalArgumentException(
                "Payment collection not found with ID: " + collectionId)))
            .then(collectionDetailRepository.findById(detailId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                    "Collection detail not found with ID: " + detailId)))
                .flatMap(detail -> {
                    if (!collectionId.equals(detail.getCollectionId())) {
                    return Mono.error(new IllegalArgumentException(
                        "Collection detail does not belong to payment collection: " + collectionId));
                    }
                    return Mono.just(detail);
                }));
    }

    private void validate(Integer collectionId, CollectionDetailDto request) {
        if (collectionId == null || collectionId <= 0) {
            throw new IllegalArgumentException("collectionId must be a valid positive number");
        }
        if (request == null) {
            throw new IllegalArgumentException("collection detail request is required");
        }
        if (request.getLineNumber() == null || request.getLineNumber() <= 0) {
            throw new IllegalArgumentException("lineNumber is required and must be greater than 0");
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new IllegalArgumentException("description is required");
        }
    }

    private CollectionDetailDto toDto(CollectionDetail detail) {
        return new CollectionDetailDto(
                detail.getDetailId(),
                detail.getCollectionId(),
                detail.getLineNumber(),
                detail.getDescription(),
                detail.getStatus(),
                detail.getCreatedAt(),
                detail.getUpdatedAt(),
                detail.getDeletedAt(),
                detail.getRestoredAt()
        );
    }
}