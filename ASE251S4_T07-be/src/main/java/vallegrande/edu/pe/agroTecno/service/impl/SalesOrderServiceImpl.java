package vallegrande.edu.pe.agroTecno.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.agroTecno.dto.OrderDetailDto;
import vallegrande.edu.pe.agroTecno.dto.SalesOrderRequestDto;
import vallegrande.edu.pe.agroTecno.dto.SalesOrderResponseDto;
import vallegrande.edu.pe.agroTecno.model.Customer;
import vallegrande.edu.pe.agroTecno.model.Formula;
import vallegrande.edu.pe.agroTecno.model.OrderDetail;
import vallegrande.edu.pe.agroTecno.model.PaymentCollection;
import vallegrande.edu.pe.agroTecno.model.SalesOrder;
import vallegrande.edu.pe.agroTecno.repository.CustomerRepository;
import vallegrande.edu.pe.agroTecno.repository.FormulaRepository;
import vallegrande.edu.pe.agroTecno.repository.OrderDetailRepository;
import vallegrande.edu.pe.agroTecno.repository.PaymentCollectionRepository;
import vallegrande.edu.pe.agroTecno.repository.SalesOrderRepository;
import vallegrande.edu.pe.agroTecno.service.SalesOrderService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class SalesOrderServiceImpl implements SalesOrderService {

    private static final Set<String> ORDER_STATUSES = Set.of("P", "C", "A");

    private final SalesOrderRepository salesOrderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CustomerRepository customerRepository;
    private final FormulaRepository formulaRepository;
    private final PaymentCollectionRepository paymentCollectionRepository;

    @Autowired
    public SalesOrderServiceImpl(SalesOrderRepository salesOrderRepository,
                                OrderDetailRepository orderDetailRepository,
                                CustomerRepository customerRepository,
                                FormulaRepository formulaRepository,
                                PaymentCollectionRepository paymentCollectionRepository) {
        this.salesOrderRepository = salesOrderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.customerRepository = customerRepository;
        this.formulaRepository = formulaRepository;
        this.paymentCollectionRepository = paymentCollectionRepository;
    }

    @Override
    public Flux<SalesOrderResponseDto> findAll() {
        return salesOrderRepository.findAll()
                .concatMap(this::enrichSalesOrder);
    }

    @Override
    public Mono<SalesOrderResponseDto> findById(Integer id) {
        if (id == null || id <= 0) {
            return Mono.error(new IllegalArgumentException("order_id must be a valid positive number"));
        }
        return salesOrderRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Sales order not found with ID: " + id)))
                .flatMap(this::enrichSalesOrder);
    }

    @Override
    @Transactional
    public Mono<SalesOrderResponseDto> create(SalesOrderRequestDto request) {
        BigDecimal totalOrder = validateAndCalculateTotal(request);
        LocalDateTime entryDate = request.getEntryDate() != null ? request.getEntryDate() : LocalDateTime.now();
        return validateCustomer(request.getMongoCustomerId())
                .flatMap(client -> validateFormulaReferences(request.getOrderDetails())
                .then(Mono.defer(() -> {
                    SalesOrder order = new SalesOrder();
                    order.setMongoCustomerId(request.getMongoCustomerId().trim());
                    order.setEntryDate(entryDate);
                    order.setTotalOrder(totalOrder);
                    order.setOrderStatus(request.getOrderStatus() != null ? request.getOrderStatus() : "A");
                    order.setCreatedAt(LocalDateTime.now());

                    return salesOrderRepository.save(order)
                            .flatMap(savedOrder -> {
                                return saveOrderDetails(savedOrder.getOrderId(), request.getOrderDetails())
                                        .collectList()
                                        .flatMap(details -> {
                                            return savePaymentCollection(savedOrder, request.getInstallmentsCount())
                                                    .flatMap(paymentCollection -> {
                                                        return salesOrderRepository.findById(savedOrder.getOrderId())
                                                                .switchIfEmpty(Mono.error(new IllegalStateException(
                                                                        "Saved sales order not found with ID: "
                                                                                + savedOrder.getOrderId())))
                                                                .map(persistedOrder -> mapToResponse(
                                                                        persistedOrder, client, details,
                                                                        paymentCollection));
                                                    });
                                        });
                            });
                })));
    }

    @Override
    @Transactional
    public Mono<SalesOrderResponseDto> update(Integer id, SalesOrderRequestDto request) {
        if (id == null || id <= 0) {
            return Mono.error(new IllegalArgumentException("order_id must be a valid positive number"));
        }
        BigDecimal totalOrder = validateAndCalculateTotal(request);
        return salesOrderRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Sales order not found with ID: " + id)))
                .flatMap(existingOrder -> validateCustomer(request.getMongoCustomerId())
                        .flatMap(client -> validateFormulaReferences(request.getOrderDetails())
                                .then(Mono.defer(() -> {
                                    existingOrder.setMongoCustomerId(request.getMongoCustomerId().trim());
                                    if (request.getEntryDate() != null) {
                                        existingOrder.setEntryDate(request.getEntryDate());
                                    }
                                    existingOrder.setTotalOrder(totalOrder);
                                    if (request.getOrderStatus() != null) {
                                        existingOrder.setOrderStatus(request.getOrderStatus());
                                    }
                                    existingOrder.setUpdatedAt(LocalDateTime.now());

                                    return salesOrderRepository.save(existingOrder)
                                            .flatMap(savedOrder -> orderDetailRepository
                                                    .deleteBySalesOrderOrderId(savedOrder.getOrderId())
                                                    .thenMany(saveOrderDetails(savedOrder.getOrderId(), request.getOrderDetails()))
                                                    .collectList()
                                                    .flatMap(details -> synchronizePaymentCollection(
                                                            savedOrder, request.getInstallmentsCount())
                                                            .map(paymentCollection -> mapToResponse(
                                                                    savedOrder, client, details, paymentCollection))));
                                }))));
    }

    private BigDecimal validateAndCalculateTotal(SalesOrderRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("sales_order request is required");
        }
        if (isBlank(request.getMongoCustomerId())) {
            throw new IllegalArgumentException("mongo_customer_id is required");
        }
        if (request.getOrderStatus() != null) {
            validateOrderStatus(request.getOrderStatus());
        }
        if (request.getInstallmentsCount() == null || request.getInstallmentsCount() <= 0) {
            throw new IllegalArgumentException("installments_count is required and must be greater than 0");
        }
        if (request.getOrderDetails() == null || request.getOrderDetails().isEmpty()) {
            throw new IllegalArgumentException("orderDetails must contain at least one item");
        }
        BigDecimal detailsTotal = BigDecimal.ZERO.setScale(2);
        for (OrderDetailDto detail : request.getOrderDetails()) {
            validateDetail(detail);
            detailsTotal = detailsTotal.add(detail.getQuantity().multiply(detail.getUnitPrice())
                    .setScale(2, RoundingMode.HALF_UP));
        }
        if (detailsTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("sum of order detail subtotals must be greater than 0");
        }
        return detailsTotal;
    }

    private void validateDetail(OrderDetailDto detail) {
        if (detail == null) {
            throw new IllegalArgumentException("order detail item cannot be null");
        }
        if (isBlank(detail.getMongoFormulaId())) {
            throw new IllegalArgumentException("mongoFormulaId is required for each order detail");
        }
        if (detail.getQuantity() == null || detail.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("quantity must be greater than 0 for each order detail");
        }
        if (detail.getUnitPrice() == null || detail.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("unitPrice must be greater than 0 for each order detail");
        }
    }

    private void validateOrderStatus(String orderStatus) {
        if (!ORDER_STATUSES.contains(orderStatus)) {
            throw new IllegalArgumentException("order_status must be P, C or A");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private Mono<Customer> validateCustomer(String mongoCustomerId) {
        if (isBlank(mongoCustomerId)) {
            return Mono.error(new IllegalArgumentException("mongo_customer_id is required"));
        }
        return customerRepository.findById(mongoCustomerId.trim())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Customer not found in MongoDB with ID: " + mongoCustomerId)));
    }

    private Mono<Void> validateFormulaReferences(List<OrderDetailDto> details) {
        return Flux.fromIterable(details)
                .concatMap(detail -> formulaRepository.findById(detail.getMongoFormulaId().trim())
                        .switchIfEmpty(Mono.error(new IllegalArgumentException(
                                "Formula not found in MongoDB with ID: " + detail.getMongoFormulaId()))))
                .then();
    }

    private Flux<OrderDetailDto> saveOrderDetails(Integer orderId, List<OrderDetailDto> detailDtos) {
        List<OrderDetail> detailsToSave = new ArrayList<>();
        for (OrderDetailDto dto : detailDtos) {
            OrderDetail detail = new OrderDetail();
            detail.setSalesOrderOrderId(orderId);
            detail.setMongoFormulaId(dto.getMongoFormulaId().trim());
            detail.setQuantity(dto.getQuantity());
            detail.setUnitPrice(dto.getUnitPrice());
            detail.setCreatedAt(LocalDateTime.now());
            detailsToSave.add(detail);
        }
        return orderDetailRepository.saveAll(Flux.fromIterable(detailsToSave))
                .map(this::mapToOrderDetailDto);
    }

    private Mono<PaymentCollection> savePaymentCollection(SalesOrder order, Integer requestedInstallmentsCount) {
        return paymentCollectionRepository.existsByOrderId(order.getOrderId())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalStateException(
                                "Payment collection already exists for sales order: " + order.getOrderId()));
                    }
                    PaymentCollection paymentCollection = new PaymentCollection();
                    paymentCollection.setOrderId(order.getOrderId());
                    paymentCollection.setMongoCustomerId(order.getMongoCustomerId());
                    paymentCollection.setTotalAmount(order.getTotalOrder());
                    paymentCollection.setInstallmentsCount(requestedInstallmentsCount);
                    paymentCollection.setPaymentStatus("P");
                    paymentCollection.setCreatedAt(LocalDateTime.now());
                    return paymentCollectionRepository.save(paymentCollection);
                });
    }

    private Mono<PaymentCollection> synchronizePaymentCollection(SalesOrder order, Integer requestedInstallmentsCount) {
        return paymentCollectionRepository.findByOrderId(order.getOrderId())
                .collectList()
                .flatMap(collections -> {
                    if (collections.size() > 1) {
                        return Mono.error(new IllegalStateException(
                                "Multiple payment collections exist for sales order: " + order.getOrderId()));
                    }
                    if (collections.isEmpty()) {
                        return savePaymentCollection(order, requestedInstallmentsCount);
                    }
                    PaymentCollection paymentCollection = collections.get(0);
                    paymentCollection.setMongoCustomerId(order.getMongoCustomerId());
                    paymentCollection.setTotalAmount(order.getTotalOrder());
                    paymentCollection.setInstallmentsCount(requestedInstallmentsCount);
                    paymentCollection.setUpdatedAt(LocalDateTime.now());
                    return paymentCollectionRepository.save(paymentCollection);
                });
    }

    private Mono<SalesOrderResponseDto> enrichSalesOrder(SalesOrder order) {
        Mono<Customer> customerMono = customerRepository.findById(order.getMongoCustomerId())
                .defaultIfEmpty(new Customer());

        Mono<List<OrderDetailDto>> detailsMono = orderDetailRepository.findBySalesOrderOrderId(order.getOrderId())
                .map(this::mapToOrderDetailDto)
                .collectList();
        Mono<PaymentCollection> paymentCollectionMono = paymentCollectionRepository.findByOrderId(order.getOrderId())
                .collectList()
                .map(list -> list.isEmpty() ? new PaymentCollection() : list.get(0));

        return Mono.zip(customerMono, detailsMono, paymentCollectionMono)
                .map(tuple -> mapToResponse(order, tuple.getT1(), tuple.getT2(),
                        tuple.getT3().getCollectionId() == null ? null : tuple.getT3()));
    }

    private OrderDetailDto mapToOrderDetailDto(OrderDetail detail) {
        return new OrderDetailDto(
                detail.getOrderDetailId(),
                detail.getSalesOrderOrderId(),
                detail.getMongoFormulaId(),
                detail.getQuantity(),
                detail.getUnitPrice(),
                detail.getSubtotal(),
                detail.getCreatedAt()
        );
    }

    private SalesOrderResponseDto mapToResponse(SalesOrder order, Customer customer, List<OrderDetailDto> details,
                                                PaymentCollection paymentCollection) {
        SalesOrderResponseDto response = new SalesOrderResponseDto(
                order.getOrderId(),
                order.getReferralGuide(),
                order.getMongoCustomerId(),
                customer != null && customer.getId() != null ? customer : null,
                order.getEntryDate(),
                order.getTotalOrder(),
                order.getOrderStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getDeletedAt(),
                order.getRestoredAt(),
                details
        );
            response.setPaymentCollection(paymentCollection);
            return response;
    }
}
