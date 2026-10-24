package vallegrande.edu.pe.agroTecno.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.agroTecno.dto.SalesOrderRequestDto;
import vallegrande.edu.pe.agroTecno.dto.SalesOrderResponseDto;
import vallegrande.edu.pe.agroTecno.service.SalesOrderService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/v1/api/sales-orders")
public class SalesOrderRest {

    private final SalesOrderService salesOrderService;

    @Autowired
    public SalesOrderRest(SalesOrderService salesOrderService) {
        this.salesOrderService = salesOrderService;
    }

    // GET - Listar todas las transacciones compuestas (cabecera + detalles)
    @GetMapping
    public Flux<SalesOrderResponseDto> findAll() {
        return salesOrderService.findAll();
    }

    // GET por ID - Obtener transacción específica y sus detalles
    @GetMapping("/{id}")
    public Mono<SalesOrderResponseDto> findById(@PathVariable Integer id) {
        return salesOrderService.findById(id);
    }

    // POST - Crear cabecera y detalles de productos
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<SalesOrderResponseDto> create(@RequestBody SalesOrderRequestDto request) {
        return salesOrderService.create(request);
    }

    // PUT - Editar transacción y sus detalles
    @PutMapping("/{id}")
    public Mono<SalesOrderResponseDto> update(@PathVariable Integer id, @RequestBody SalesOrderRequestDto request) {
        return salesOrderService.update(id, request);
    }
}
