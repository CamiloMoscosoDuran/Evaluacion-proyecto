package vallegrande.edu.pe.agroTecno.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.agroTecno.model.PaymentCollection;
import vallegrande.edu.pe.agroTecno.service.PaymentCollectionService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/v1/api/payment-collections")
public class PaymentCollectionRest {

    private final PaymentCollectionService paymentCollectionService;

    @Autowired
    public PaymentCollectionRest(PaymentCollectionService paymentCollectionService) {
        this.paymentCollectionService = paymentCollectionService;
    }

    @GetMapping
    public Flux<PaymentCollection> findAll() { return paymentCollectionService.findAll(); }

    @GetMapping("/order/{orderId}")
    public Flux<PaymentCollection> findByOrderId(@PathVariable Integer orderId) { return paymentCollectionService.findByOrderId(orderId); }

    @GetMapping("/status/{status}")
    public Flux<PaymentCollection> findByPaymentStatus(@PathVariable String status) { return paymentCollectionService.findByPaymentStatus(status); }

    @GetMapping("/{id}")
    public Mono<PaymentCollection> findById(@PathVariable Integer id) { return paymentCollectionService.findById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PaymentCollection> save(@RequestBody PaymentCollection paymentCollection) { return paymentCollectionService.save(paymentCollection); }

    @PutMapping("/{id}")
    public Mono<PaymentCollection> update(@PathVariable Integer id, @RequestBody PaymentCollection paymentCollection) { return paymentCollectionService.update(id, paymentCollection); }

}
