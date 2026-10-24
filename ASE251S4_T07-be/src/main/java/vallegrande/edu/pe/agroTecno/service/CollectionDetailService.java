package vallegrande.edu.pe.agroTecno.service;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import vallegrande.edu.pe.agroTecno.dto.CollectionDetailDto;

public interface CollectionDetailService {
    Flux<CollectionDetailDto> findByCollectionId(Integer collectionId);
    Mono<CollectionDetailDto> findById(Integer collectionId, Integer detailId);
    Mono<CollectionDetailDto> create(Integer collectionId, CollectionDetailDto request);
    Mono<CollectionDetailDto> update(Integer collectionId, Integer detailId, CollectionDetailDto request);
    Mono<CollectionDetailDto> delete(Integer collectionId, Integer detailId);
    Mono<CollectionDetailDto> restore(Integer collectionId, Integer detailId);
}