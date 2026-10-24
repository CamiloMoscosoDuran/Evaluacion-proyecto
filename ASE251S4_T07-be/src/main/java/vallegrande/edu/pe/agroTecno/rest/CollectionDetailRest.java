package vallegrande.edu.pe.agroTecno.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;
import vallegrande.edu.pe.agroTecno.dto.CollectionDetailDto;
import vallegrande.edu.pe.agroTecno.service.CollectionDetailService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/v1/api/payment-collections/{collectionId}/details")
public class CollectionDetailRest {

    private final CollectionDetailService collectionDetailService;

    public CollectionDetailRest(CollectionDetailService collectionDetailService) {
        this.collectionDetailService = collectionDetailService;
    }

    @GetMapping
    public reactor.core.publisher.Flux<CollectionDetailDto> findByCollectionId(@PathVariable Integer collectionId) {
        return collectionDetailService.findByCollectionId(collectionId);
    }

    @GetMapping("/{detailId}")
    public Mono<CollectionDetailDto> findById(@PathVariable Integer collectionId,
                                              @PathVariable Integer detailId) {
        return collectionDetailService.findById(collectionId, detailId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CollectionDetailDto> create(@PathVariable Integer collectionId,
                                            @RequestBody CollectionDetailDto request) {
        return collectionDetailService.create(collectionId, request);
    }

    @PutMapping("/{detailId}")
    public Mono<CollectionDetailDto> update(@PathVariable Integer collectionId,
                                            @PathVariable Integer detailId,
                                            @RequestBody CollectionDetailDto request) {
        return collectionDetailService.update(collectionId, detailId, request);
    }

    @PatchMapping("/{detailId}/delete")
    public Mono<CollectionDetailDto> delete(@PathVariable Integer collectionId,
                                            @PathVariable Integer detailId) {
        return collectionDetailService.delete(collectionId, detailId);
    }

    @PatchMapping("/{detailId}/restore")
    public Mono<CollectionDetailDto> restore(@PathVariable Integer collectionId,
                                             @PathVariable Integer detailId) {
        return collectionDetailService.restore(collectionId, detailId);
    }
}