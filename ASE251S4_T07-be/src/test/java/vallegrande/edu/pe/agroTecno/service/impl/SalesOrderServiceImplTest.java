package vallegrande.edu.pe.agroTecno.service.impl;

import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import vallegrande.edu.pe.agroTecno.dto.OrderDetailDto;
import vallegrande.edu.pe.agroTecno.dto.SalesOrderRequestDto;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalesOrderServiceImplTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private FormulaRepository formulaRepository;

        @Mock
        private PaymentCollectionRepository paymentCollectionRepository;

    @InjectMocks
    private SalesOrderServiceImpl salesOrderService;

    @Test
        void createUsesDatabaseGeneratedReferralAndAssociatesOrderDetail() {
        Customer customer = new Customer();
        customer.setId("CUS001");
        Formula formula = new Formula();
        OrderDetailDto orderDetail = new OrderDetailDto();
        orderDetail.setMongoFormulaId("FOR001");
        orderDetail.setQuantity(new BigDecimal("2.00"));
        orderDetail.setUnitPrice(new BigDecimal("50.00"));
        SalesOrderRequestDto request = new SalesOrderRequestDto(
                "CUS001", LocalDateTime.of(2026, 10, 1, 10, 0), new BigDecimal("999.99"), "P", List.of(orderDetail));
        request.setInstallmentsCount(6);

        when(customerRepository.findById("CUS001")).thenReturn(Mono.just(customer));
        when(formulaRepository.findById("FOR001")).thenReturn(Mono.just(formula));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenAnswer(invocation -> {
            SalesOrder savedOrder = invocation.getArgument(0);
            savedOrder.setOrderId(25);
            return Mono.just(savedOrder);
        });
        SalesOrder persistedOrder = new SalesOrder();
        persistedOrder.setOrderId(25);
        persistedOrder.setReferralGuide("REF01102026-25");
        persistedOrder.setMongoCustomerId("CUS001");
        persistedOrder.setEntryDate(request.getEntryDate());
        persistedOrder.setTotalOrder(new BigDecimal("100.00"));
        persistedOrder.setOrderStatus("P");
        when(salesOrderRepository.findById(25)).thenReturn(Mono.just(persistedOrder));
        when(orderDetailRepository.saveAll(any(Publisher.class))).thenAnswer(invocation ->
                Flux.from((Publisher<OrderDetail>) invocation.getArgument(0)));
                when(paymentCollectionRepository.existsByOrderId(25)).thenReturn(Mono.just(false));
                when(paymentCollectionRepository.save(any(PaymentCollection.class))).thenAnswer(invocation -> {
                        PaymentCollection paymentCollection = invocation.getArgument(0);
                        paymentCollection.setCollectionId(80);
                        return Mono.just(paymentCollection);
                });

        StepVerifier.create(salesOrderService.create(request))
                .assertNext(saved -> {
                    assertEquals(25, saved.getOrderId());
                    assertEquals("REF01102026-25", saved.getReferralGuide());
                    assertEquals(new BigDecimal("100.00"), saved.getTotalOrder());
                    assertEquals(1, saved.getOrderDetails().size());
                    assertEquals(25, saved.getOrderDetails().get(0).getSalesOrderOrderId());
                    assertEquals("FOR001", saved.getOrderDetails().get(0).getMongoFormulaId());
                                    assertEquals(80, saved.getPaymentCollection().getCollectionId());
                                    assertEquals(25, saved.getPaymentCollection().getOrderId());
                                    assertEquals("CUS001", saved.getPaymentCollection().getMongoCustomerId());
                                    assertEquals(new BigDecimal("100.00"), saved.getPaymentCollection().getTotalAmount());
                                    assertEquals("P", saved.getPaymentCollection().getPaymentStatus());
                })
                .verifyComplete();
    }

    @Test
    void createDoesNotSaveHeaderWhenFormulaDoesNotExist() {
        Customer customer = new Customer();
        customer.setId("CUS001");
        OrderDetailDto orderDetail = new OrderDetailDto();
        orderDetail.setMongoFormulaId("FOR999");
        orderDetail.setQuantity(new BigDecimal("1.00"));
        orderDetail.setUnitPrice(new BigDecimal("50.00"));
        SalesOrderRequestDto request = new SalesOrderRequestDto(
                "CUS001", LocalDateTime.of(2026, 10, 1, 10, 0), new BigDecimal("50.00"), "P", List.of(orderDetail));
        request.setInstallmentsCount(1);

        when(customerRepository.findById("CUS001")).thenReturn(Mono.just(customer));
        when(formulaRepository.findById("FOR999")).thenReturn(Mono.empty());

        StepVerifier.create(salesOrderService.create(request))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException
                        && error.getMessage().contains("FOR999"))
                .verify();

        verify(salesOrderRepository, never()).save(any(SalesOrder.class));
        verify(orderDetailRepository, never()).saveAll(any(Publisher.class));
    }

        @Test
        void createRejectsRequestWithoutOrderDetails() {
                SalesOrderRequestDto request = new SalesOrderRequestDto(
                                "CUS001", LocalDateTime.of(2026, 10, 1, 10, 0), new BigDecimal("100.00"), "P", List.of());
                request.setInstallmentsCount(1);

                assertThrows(IllegalArgumentException.class, () -> salesOrderService.create(request));

                verify(salesOrderRepository, never()).save(any(SalesOrder.class));
        }

    @Test
    void requestReadsInstallmentsCountUsingSnakeCaseJsonName() throws Exception {
        SalesOrderRequestDto request = new ObjectMapper()
                .readValue("{\"installments_count\":6}", SalesOrderRequestDto.class);

        assertEquals(6, request.getInstallmentsCount());
    }

        @Test
        void createRejectsRequestWithoutInstallmentsCount() {
                OrderDetailDto detail = new OrderDetailDto();
                detail.setMongoFormulaId("FOR001");
                detail.setQuantity(BigDecimal.ONE);
                detail.setUnitPrice(BigDecimal.TEN);
                SalesOrderRequestDto request = new SalesOrderRequestDto(
                                "CUS001", null, null, "P", List.of(detail));

                assertThrows(IllegalArgumentException.class, () -> salesOrderService.create(request));

                verify(salesOrderRepository, never()).save(any(SalesOrder.class));
        }
}