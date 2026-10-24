package vallegrande.edu.pe.agroTecno.service.impl;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.agroTecno.dto.OrderDetailDto;
import vallegrande.edu.pe.agroTecno.model.OrderDetail;
import vallegrande.edu.pe.agroTecno.model.SalesOrder;
import vallegrande.edu.pe.agroTecno.repository.OrderDetailRepository;
import vallegrande.edu.pe.agroTecno.repository.SalesOrderRepository;
import vallegrande.edu.pe.agroTecno.service.OrderDetailService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class OrderDetailServiceImpl implements OrderDetailService {

    private final OrderDetailRepository orderDetailRepository;
    private final SalesOrderRepository salesOrderRepository;

    public OrderDetailServiceImpl(OrderDetailRepository orderDetailRepository,
                                 SalesOrderRepository salesOrderRepository) {
        this.orderDetailRepository = orderDetailRepository;
        this.salesOrderRepository = salesOrderRepository;
    }

    @Override
    public Flux<OrderDetailDto> findBySalesOrderId(Integer salesOrderOrderId) {
        return salesOrderRepository.findById(salesOrderOrderId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Sales order not found with ID: " + salesOrderOrderId)))
                .thenMany(orderDetailRepository.findBySalesOrderOrderId(salesOrderOrderId))
                .map(this::toDto);
    }

    @Override
    public Mono<OrderDetailDto> create(Integer salesOrderOrderId, OrderDetailDto request) {
        if (request == null) {
            return Mono.error(new IllegalArgumentException("Order detail request is required"));
        }

        return salesOrderRepository.findById(salesOrderOrderId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Sales order not found with ID: " + salesOrderOrderId)))
                .flatMap(order -> {
                    OrderDetail entity = new OrderDetail();
                    entity.setSalesOrderOrderId(order.getOrderId());
                    entity.setMongoFormulaId(request.getMongoFormulaId());
                    entity.setQuantity(request.getQuantity());
                    entity.setUnitPrice(request.getUnitPrice());
                    entity.setCreatedAt(LocalDateTime.now());

                    return orderDetailRepository.save(entity)
                            .map(this::toDto);
                });
    }

    @Override
    public Mono<OrderDetailDto> update(Integer orderDetailId, OrderDetailDto request) {
        if (request == null) {
            return Mono.error(new IllegalArgumentException("Order detail request is required"));
        }

        return orderDetailRepository.findById(orderDetailId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Order detail not found with ID: " + orderDetailId)))
                .flatMap(existing -> {
                    existing.setMongoFormulaId(request.getMongoFormulaId());
                    if (request.getQuantity() != null) {
                        existing.setQuantity(request.getQuantity());
                    }
                    if (request.getUnitPrice() != null) {
                        existing.setUnitPrice(request.getUnitPrice());
                    }
                    return orderDetailRepository.save(existing).map(this::toDto);
                });
    }

    private OrderDetailDto toDto(OrderDetail entity) {
        if (entity == null) {
            return null;
        }
        return new OrderDetailDto(
                entity.getOrderDetailId(),
                entity.getSalesOrderOrderId(),
                entity.getMongoFormulaId(),
                entity.getQuantity(),
                entity.getUnitPrice(),
                entity.getSubtotal(),
                entity.getCreatedAt()
        );
    }
}
