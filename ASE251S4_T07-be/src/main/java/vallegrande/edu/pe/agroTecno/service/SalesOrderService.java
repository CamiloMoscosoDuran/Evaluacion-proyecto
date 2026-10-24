package vallegrande.edu.pe.agroTecno.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.agroTecno.dto.SalesOrderRequestDto;
import vallegrande.edu.pe.agroTecno.dto.SalesOrderResponseDto;

public interface SalesOrderService {

    Flux<SalesOrderResponseDto> findAll();

    Mono<SalesOrderResponseDto> findById(Integer id);

    Mono<SalesOrderResponseDto> create(SalesOrderRequestDto request);

    Mono<SalesOrderResponseDto> update(Integer id, SalesOrderRequestDto request);
}
