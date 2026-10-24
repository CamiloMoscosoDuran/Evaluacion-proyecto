package vallegrande.edu.pe.agroTecno.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.agroTecno.model.Installment;
import vallegrande.edu.pe.agroTecno.service.InstallmentService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/v1/api")
public class InstallmentRest {

    private final InstallmentService installmentService;

    public InstallmentRest(InstallmentService installmentService) {
        this.installmentService = installmentService;
    }

    @GetMapping("/installments")
    public Flux<Installment> findAll() {
        return installmentService.findAll();
    }

    @GetMapping("/installments/collection/{collectionId}")
    public Flux<Installment> findByCollectionId(@PathVariable Integer collectionId) {
        return installmentService.findByCollectionId(collectionId);
    }

    @GetMapping("/installments/status/{status}")
    public Flux<Installment> findByStatus(@PathVariable String status) {
        return installmentService.findByStatus(status);
    }

    @GetMapping("/installments/{id}")
    public Mono<Installment> findById(@PathVariable Integer id) {
        return installmentService.findById(id);
    }

    @PostMapping("/payment-collections/{collectionId}/installments")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Installment> save(@PathVariable Integer collectionId, @RequestBody Installment installment) {
        return installmentService.save(collectionId, installment);
    }

    @PutMapping("/installments/{id}")
    public Mono<Installment> update(@PathVariable Integer id, @RequestBody Installment installment) {
        return installmentService.update(id, installment);
    }

    @PatchMapping("/installments/{id}/delete")
    public Mono<Installment> delete(@PathVariable Integer id) {
        return installmentService.delete(id);
    }

    @PatchMapping("/installments/{id}/restore")
    public Mono<Installment> restore(@PathVariable Integer id) {
        return installmentService.restore(id);
    }
}
