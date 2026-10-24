package vallegrande.edu.pe.agroTecno.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import vallegrande.edu.pe.agroTecno.model.Customer;
import vallegrande.edu.pe.agroTecno.repository.CustomerRepository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerServiceImpl customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerServiceImpl(customerRepository);
    }

    @Test
    void saveGeneratesCorrelativeCustomerIdWhenNotProvided() {
        Customer customer = new Customer();
        customer.setFirstName("Daniel");
        customer.setLastName("Huaman");
        customer.setDocumentNumber("78451236");

        when(customerRepository.findAll()).thenReturn(Flux.empty());
        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> Mono.just((Customer) invocation.getArgument(0)));

        StepVerifier.create(customerService.save(customer))
                .assertNext(saved -> {
                    assertEquals("CUS001", saved.getId());
                    assertTrue(saved.getStatus());
                    assertEquals("A", saved.getEstado());
                    assertNotNull(saved.getCreatedAt());
                    assertNotNull(saved.getRegistrationDate());
                })
                .verifyComplete();
    }

    @Test
    void jacksonCanSerializeAndDeserializeCustomer() throws Exception {
        Customer customer = new Customer();
        customer.setId("CUS001");
        customer.setFirstName("Daniel");
        customer.setLastName("Huaman");
        customer.setPhone(List.of("987654321", "912345678"));
        customer.setStatus(true);

        com.fasterxml.jackson.databind.ObjectMapper mapper = new vallegrande.edu.pe.agroTecno.JacksonConfig().objectMapper();
        String json = mapper.writeValueAsString(customer);
        Customer read = mapper.readValue(json, Customer.class);

        assertEquals("CUS001", read.getId());
        assertEquals(2, read.getPhone().size());
        assertTrue(read.getStatus());
    }

    @Test
    void saveIncrementsExistingCorrelativeCustomerId() {
        Customer existing = new Customer();
        existing.setId("CUS005");

        Customer newCustomer = new Customer();
        newCustomer.setFirstName("Valeria");
        newCustomer.setLastName("Rojas");
        newCustomer.setDocumentNumber("74589632");

        when(customerRepository.findAll()).thenReturn(Flux.just(existing));
        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> Mono.just((Customer) invocation.getArgument(0)));

        StepVerifier.create(customerService.save(newCustomer))
                .assertNext(saved -> assertEquals("CUS006", saved.getId()))
                .verifyComplete();
    }

    @Test
    void importSavesTenCsvRows() {
        String csv = validCsv(10);
        when(customerRepository.findAll()).thenReturn(Flux.empty());
        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> Mono.just((Customer) invocation.getArgument(0)));

        StepVerifier.create(customerService.importCsv(csv.getBytes(StandardCharsets.UTF_8)))
                .assertNext(imported -> assertEquals(10, imported.size()))
                .verifyComplete();

        verify(customerRepository, org.mockito.Mockito.times(10)).save(any(Customer.class));
    }

    @Test
    void importRejectsFewerThanTenRows() {
        String csv = validCsv(9);
        StepVerifier.create(customerService.importCsv(csv.getBytes(StandardCharsets.UTF_8)))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void exportsRejectDatabaseWithFewerThanTenRows() {
        when(customerRepository.findAll()).thenReturn(Flux.just(new Customer()));

        StepVerifier.create(customerService.exportExcel())
                .expectError(ResponseStatusException.class)
                .verify();

        StepVerifier.create(customerService.exportPdf())
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void exportsExcelAndPdfWhenDatabaseHasTenRows() {
        List<Customer> customers = IntStream.rangeClosed(1, 10)
                .mapToObj(i -> {
                    Customer c = new Customer();
                    c.setId(String.format("CUS%03d", i));
                    c.setFirstName("Nombre " + i);
                    c.setLastName("Apellido " + i);
                    c.setDocumentType("DNI");
                    c.setDocumentNumber("7000000" + i);
                    c.setPhone("91234511" + i);
                    c.setEmail("cliente" + i + "@gmail.com");
                    c.setRegistrationDate(LocalDate.now());
                    c.setStatus(true);
                    return c;
                })
                .toList();

        when(customerRepository.findAll()).thenReturn(Flux.fromIterable(customers));

        StepVerifier.create(customerService.exportExcel())
                .assertNext(content -> {
                    // Excel files begin with ZIP header 'PK'
                    assertEquals((byte) 'P', content[0]);
                    assertEquals((byte) 'K', content[1]);
                })
                .verifyComplete();

        StepVerifier.create(customerService.exportPdf())
                .assertNext(content -> assertEquals("%PDF", new String(content, 0, 4, StandardCharsets.US_ASCII)))
                .verifyComplete();
    }

    @Test
    void exportByStateFiltersBeforeCheckingMinimum() {
        List<Customer> customers = IntStream.rangeClosed(1, 19)
                .mapToObj(i -> {
                    Customer c = new Customer();
                    c.setId(String.format("CUS%03d", i));
                    c.setFirstName("Nombre " + i);
                    c.setLastName("Apellido " + i);
                    c.setDocumentType("DNI");
                    c.setDocumentNumber("7000000" + i);
                    c.setStatus(i <= 10);
                    return c;
                })
                .toList();

        when(customerRepository.findAll()).thenReturn(Flux.fromIterable(customers));

        StepVerifier.create(customerService.exportPdf("A", "ACTIVOS"))
                .assertNext(content -> assertEquals("%PDF", new String(content, 0, 4, StandardCharsets.US_ASCII)))
                .verifyComplete();

        StepVerifier.create(customerService.exportExcel("I"))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    private String validCsv(int rows) {
        StringBuilder csv = new StringBuilder("first_name,last_name,document_type,document_number,phone,email,registration_date,status\n");
        for (int i = 1; i <= rows; i++) {
            csv.append("Daniel").append(i).append(",")
                    .append("Huaman").append(i).append(",")
                    .append("DNI,")
                    .append(String.format("7%07d", i)).append(",")
                    .append(String.format("9%08d", i)).append(",")
                    .append("cliente").append(i).append("@gmail.com,")
                    .append("2026-01-01,")
                    .append("true\n");
        }
        return csv.toString();
    }
}
