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
import vallegrande.edu.pe.agroTecno.model.Inventory;
import vallegrande.edu.pe.agroTecno.model.Supply;
import vallegrande.edu.pe.agroTecno.repository.SupplyRepository;

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
class SupplyServiceImplTest {

    @Mock
    private SupplyRepository supplyRepository;

    private SupplyServiceImpl supplyService;

    @BeforeEach
    void setUp() {
        supplyService = new SupplyServiceImpl(supplyRepository);
    }

    @Test
    void saveGeneratesCorrelativeSupplyIdWhenNotProvided() {
        Supply supply = new Supply();
        supply.setName("Nitrato de Calcio");
        supply.setCategory("Fertilizantes");
        supply.setUnit("kg");
        supply.setLocation("Almacén A - Estante 1");
        supply.setInventory(new Inventory(100.0, 10.0, LocalDate.of(2028, 12, 31)));

        when(supplyRepository.findAll()).thenReturn(Flux.empty());
        when(supplyRepository.save(any(Supply.class)))
                .thenAnswer(invocation -> Mono.just((Supply) invocation.getArgument(0)));

        StepVerifier.create(supplyService.save(supply))
                .assertNext(saved -> {
                    assertEquals("SUP001", saved.getId());
                    assertEquals("Fertilizantes", saved.getCategory());
                    assertTrue(saved.getStatus());
                    assertNotNull(saved.getCreatedAt());
                    assertNotNull(saved.getUpdateAt());
                })
                .verifyComplete();
    }

    @Test
    void saveIncrementsExistingCorrelativeSupplyId() {
        Supply existing = new Supply();
        existing.setId("SUP005");

        Supply newSupply = new Supply();
        newSupply.setName("Sulfato de Cobre");
        newSupply.setUnit("kg");
        newSupply.setLocation("Almacén B");
        newSupply.setInventory(new Inventory(50.0, 5.0, LocalDate.of(2028, 12, 31)));

        when(supplyRepository.findAll()).thenReturn(Flux.just(existing));
        when(supplyRepository.save(any(Supply.class)))
                .thenAnswer(invocation -> Mono.just((Supply) invocation.getArgument(0)));

        StepVerifier.create(supplyService.save(newSupply))
                .assertNext(saved -> {
                    assertEquals("SUP006", saved.getId());
                    assertEquals("General", saved.getCategory()); // Default category
                })
                .verifyComplete();
    }

    @Test
    void importSavesTenCsvRows() {
        String csv = validCsv(10);
        when(supplyRepository.findAll()).thenReturn(Flux.empty());
        when(supplyRepository.save(any(Supply.class)))
                .thenAnswer(invocation -> Mono.just((Supply) invocation.getArgument(0)));

        StepVerifier.create(supplyService.importCsv(csv.getBytes(StandardCharsets.UTF_8)))
                .assertNext(imported -> assertEquals(10, imported.size()))
                .verifyComplete();

        verify(supplyRepository, org.mockito.Mockito.times(10)).save(any(Supply.class));
    }

    @Test
    void importRejectsFewerThanTenRows() {
        String csv = validCsv(9);
        StepVerifier.create(supplyService.importCsv(csv.getBytes(StandardCharsets.UTF_8)))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void exportsRejectDatabaseWithFewerThanTenRows() {
        when(supplyRepository.findAll()).thenReturn(Flux.just(new Supply()));

        StepVerifier.create(supplyService.exportExcel())
                .expectError(ResponseStatusException.class)
                .verify();

        StepVerifier.create(supplyService.exportPdf())
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void exportsExcelAndPdfWhenDatabaseHasTenRows() {
        List<Supply> supplies = IntStream.rangeClosed(1, 10)
                .mapToObj(i -> {
                    Supply s = new Supply();
                    s.setId(String.format("SUP%03d", i));
                    s.setName("Insumo " + i);
                    s.setDescription("Descripción " + i);
                    s.setCategory("Fertilizantes");
                    s.setUnit("kg");
                    s.setLocation("Almacén A");
                    s.setInventory(new Inventory(100.0 * i, 10.0 * i, LocalDate.of(2028, 1, 1)));
                    s.setStatus(true);
                    return s;
                })
                .toList();

        when(supplyRepository.findAll()).thenReturn(Flux.fromIterable(supplies));

        StepVerifier.create(supplyService.exportExcel())
                .assertNext(content -> {
                    assertEquals((byte) 'P', content[0]);
                    assertEquals((byte) 'K', content[1]);
                })
                .verifyComplete();

        StepVerifier.create(supplyService.exportPdf())
                .assertNext(content -> assertEquals("%PDF", new String(content, 0, 4, StandardCharsets.US_ASCII)))
                .verifyComplete();
    }

    private String validCsv(int rows) {
        StringBuilder csv = new StringBuilder("name,category,description,unit,location,current_stock,minimum_stock,expiration_date\n");
        for (int i = 1; i <= rows; i++) {
            csv.append("Insumo ").append(i).append(",")
                    .append("Fertilizantes,")
                    .append("Descripcion ").append(i).append(",")
                    .append("kg,")
                    .append("Almacen A,")
                    .append("100.0,")
                    .append("10.0,")
                    .append("2028-12-31\n");
        }
        return csv.toString();
    }
}
