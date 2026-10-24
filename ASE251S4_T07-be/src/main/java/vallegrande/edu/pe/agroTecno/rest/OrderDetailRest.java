package vallegrande.edu.pe.agroTecno.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.agroTecno.dto.OrderDetailDto;
import vallegrande.edu.pe.agroTecno.service.OrderDetailService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/v1/api/order-details")
public class OrderDetailRest {

    private final OrderDetailService orderDetailService;

    public OrderDetailRest(OrderDetailService orderDetailService) {
        this.orderDetailService = orderDetailService;
    }

    @GetMapping("/sales-orders/{salesOrderId}/details")
    public Flux<OrderDetailDto> findBySalesOrderId(@PathVariable Integer salesOrderId) {
        return orderDetailService.findBySalesOrderId(salesOrderId);
    }

    @PostMapping("/sales-orders/{salesOrderId}/details")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<OrderDetailDto> create(@PathVariable Integer salesOrderId,
                                      @RequestBody OrderDetailDto request) {
        return orderDetailService.create(salesOrderId, request);
    }

    @PutMapping("/sales-orders/details/{orderDetailId}")
    public Mono<OrderDetailDto> update(@PathVariable Integer orderDetailId,
                                      @RequestBody OrderDetailDto request) {
        return orderDetailService.update(orderDetailId, request);
    }
}
