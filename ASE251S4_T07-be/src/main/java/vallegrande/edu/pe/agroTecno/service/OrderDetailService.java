package vallegrande.edu.pe.agroTecno.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.agroTecno.dto.OrderDetailDto;

public interface OrderDetailService {

    Flux<OrderDetailDto> findBySalesOrderId(Integer salesOrderOrderId);

    Mono<OrderDetailDto> create(Integer salesOrderOrderId, OrderDetailDto request);

    Mono<OrderDetailDto> update(Integer orderDetailId, OrderDetailDto request);
}
