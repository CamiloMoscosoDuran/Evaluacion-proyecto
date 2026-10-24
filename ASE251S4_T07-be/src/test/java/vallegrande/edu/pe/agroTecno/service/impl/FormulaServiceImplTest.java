package vallegrande.edu.pe.agroTecno.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import org.springframework.web.server.ResponseStatusException;
import vallegrande.edu.pe.agroTecno.model.Formula;
import vallegrande.edu.pe.agroTecno.model.Supply;
import vallegrande.edu.pe.agroTecno.JacksonConfig;
import vallegrande.edu.pe.agroTecno.repository.FormulaRepository;
import vallegrande.edu.pe.agroTecno.repository.SupplyRepository;

import java.util.List;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FormulaServiceImplTest {

    @Mock
    private FormulaRepository formulaRepository;

    @Mock
    private SupplyRepository supplyRepository;

    private FormulaServiceImpl formulaService;

        @BeforeEach
        void setUp() {
                formulaService = new FormulaServiceImpl(formulaRepository, supplyRepository,
                                new JacksonConfig().objectMapper());
        }

    @Test
    void saveFillsSupplyNameFromExistingSupply() {
        Formula formula = formulaWithIngredient("SUP001", "Nombre enviado por el cliente");
        Supply supply = new Supply();
        supply.setId("SUP001");
        supply.setName("Nitrato de calcio");

        when(formulaRepository.findAll()).thenReturn(Flux.empty());
        when(supplyRepository.findById("SUP001")).thenReturn(Mono.just(supply));
        when(formulaRepository.save(any(Formula.class)))
                .thenAnswer(invocation -> Mono.just((Formula) invocation.getArgument(0)));

        StepVerifier.create(formulaService.save(formula))
                .assertNext(saved -> {
                    assertEquals("SUP001", saved.getRecipe().getIngredients().get(0).getSupplyId());
                    assertEquals("Nitrato de calcio", saved.getRecipe().getIngredients().get(0).getSupplyName());
                })
                .verifyComplete();
    }

    @Test
    void updateFillsSupplyNameFromExistingSupply() {
        Formula existing = new Formula();
        Formula request = formulaWithIngredient("SUP001", null);
        Supply supply = new Supply();
        supply.setId("SUP001");
        supply.setName("Nitrato de calcio");

        when(formulaRepository.findById("FOR001")).thenReturn(Mono.just(existing));
        when(supplyRepository.findById("SUP001")).thenReturn(Mono.just(supply));
        when(formulaRepository.save(any(Formula.class)))
                .thenAnswer(invocation -> Mono.just((Formula) invocation.getArgument(0)));

        StepVerifier.create(formulaService.update("FOR001", request))
                .assertNext(updated -> assertEquals(
                        "Nitrato de calcio", updated.getRecipe().getIngredients().get(0).getSupplyName()))
                .verifyComplete();
    }

    @Test
    void doesNotSaveFormulaWhenSupplyDoesNotExist() {
        Formula formula = formulaWithIngredient("SUP999", null);
                when(formulaRepository.findAll()).thenReturn(Flux.empty());
        when(supplyRepository.findById("SUP999")).thenReturn(Mono.empty());

        StepVerifier.create(formulaService.save(formula))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException
                        && error.getMessage().contains("SUP999"))
                .verify();

        verify(formulaRepository, never()).save(any(Formula.class));
    }

        @Test
        void saveGeneratesNextIdAfterHighestExistingFormulaId() {
                Formula existingFormula = new Formula();
                existingFormula.setId("FOR020");
                Formula newFormula = new Formula();
                newFormula.setId("FOR999");

                when(formulaRepository.findAll()).thenReturn(Flux.just(existingFormula));
                when(formulaRepository.save(any(Formula.class)))
                                .thenAnswer(invocation -> Mono.just((Formula) invocation.getArgument(0)));

                StepVerifier.create(formulaService.save(newFormula))
                                .assertNext(saved -> assertEquals("FOR021", saved.getId()))
                                .verifyComplete();
        }

    @Test
    void importRejectsCsvWithFewerThanTenRowsWithoutSaving() {
                String csv = validCsv(1);

        StepVerifier.create(formulaService.importCsv(csv.getBytes(StandardCharsets.UTF_8)))
                .expectErrorMatches(error -> error instanceof ResponseStatusException
                        && ((ResponseStatusException) error).getReason().contains("al menos 10"))
                .verify();

        verify(formulaRepository, never()).save(any(Formula.class));
    }

        @Test
        void importRejectsCsvWithoutRequiredRecipe() {
                StringBuilder csv = new StringBuilder("name,standard_batch,unit\n");
                for (int index = 1; index <= 10; index++) {
                        csv.append("Formula ").append(index).append(",10,kg\n");
                }

                StepVerifier.create(formulaService.importCsv(csv.toString().getBytes(StandardCharsets.UTF_8)))
                                .expectErrorMatches(error -> error instanceof ResponseStatusException
                                                && ((ResponseStatusException) error).getReason().contains("recipe con version e ingredients"))
                                .verify();

                verify(formulaRepository, never()).save(any(Formula.class));
        }

    @Test
    void importSavesTenCsvRows() {
                String csv = validCsv(10).replace("name,standard_batch,unit,recipe",
                                "NAME,STANDARD_BATCH,UNIT,RECIPE");
        when(formulaRepository.findAll()).thenReturn(Flux.empty());
        when(formulaRepository.save(any(Formula.class)))
                .thenAnswer(invocation -> Mono.just((Formula) invocation.getArgument(0)));

                StepVerifier.create(formulaService.importCsv(csv.getBytes(StandardCharsets.UTF_8)))
                .assertNext(imported -> assertEquals(10, imported.size()))
                .verifyComplete();

        verify(formulaRepository, org.mockito.Mockito.times(10)).save(any(Formula.class));
    }

    @Test
    void importSavesFormulaWithFormulaInventoryArray() {
        StringBuilder csv = new StringBuilder("name,standard_batch,unit,recipe,formula_inventory\n");
        String recipe = "{\"version\":\"1.0\",\"ingredients\":[]}";
        String csvRecipe = "\"" + recipe.replace("\"", "\"\"") + "\"";
        String inventory = "[{\"gallon_type\":1,\"stock_quantity\":80,\"last_update\":\"2026-10-18\"},{\"gallon_type\":20,\"stock_quantity\":15,\"last_update\":\"2026-10-18\"}]";
        String csvInventory = "\"" + inventory.replace("\"", "\"\"") + "\"";

        for (int index = 1; index <= 10; index++) {
            csv.append("Formula ").append(index).append(",10,kg,")
                    .append(csvRecipe).append(",")
                    .append(csvInventory).append('\n');
        }

        when(formulaRepository.findAll()).thenReturn(Flux.empty());
        when(formulaRepository.save(any(Formula.class)))
                .thenAnswer(invocation -> Mono.just((Formula) invocation.getArgument(0)));

        StepVerifier.create(formulaService.importCsv(csv.toString().getBytes(StandardCharsets.UTF_8)))
                .assertNext(imported -> {
                    assertEquals(10, imported.size());
                    Formula first = imported.get(0);
                    assertEquals(2, first.getFormulaInventory().size());
                    assertEquals(1, first.getFormulaInventory().get(0).getGallonType());
                    assertEquals(80, first.getFormulaInventory().get(0).getStockQuantity());
                    assertEquals(20, first.getFormulaInventory().get(1).getGallonType());
                    assertEquals(15, first.getFormulaInventory().get(1).getStockQuantity());
                })
                .verifyComplete();
    }

        private String validCsv(int rows) {
                StringBuilder csv = new StringBuilder("name,standard_batch,unit,recipe\n");
                String recipe = "{\"version\":\"1.0\",\"ingredients\":[]}";
                String csvRecipe = "\"" + recipe.replace("\"", "\"\"") + "\"";
                for (int index = 1; index <= rows; index++) {
                        csv.append("Formula ").append(index).append(",10,kg,")
                                        .append(csvRecipe).append('\n');
                }
                return csv.toString();
        }

    @Test
    void exportsRejectDatabaseWithFewerThanTenRows() {
        when(formulaRepository.findAll()).thenReturn(Flux.just(new Formula()));

        StepVerifier.create(formulaService.exportExcel())
                .expectError(ResponseStatusException.class)
                .verify();
        StepVerifier.create(formulaService.exportPdf())
                .expectError(ResponseStatusException.class)
                .verify();
    }

        @Test
        void exportsExcelAndPdfWhenDatabaseHasTenRows() {
                List<Formula> formulas = java.util.stream.IntStream.rangeClosed(1, 10)
                                .mapToObj(index -> {
                                        Formula formula = new Formula();
                                        formula.setId(String.format("FOR%03d", index));
                                        formula.setName("Formula " + index);
                                        formula.setStandardBatch(10);
                                        formula.setUnit("kg");
                                        formula.setStatus(true);
                                        return formula;
                                })
                                .toList();
                when(formulaRepository.findAll()).thenReturn(Flux.fromIterable(formulas));

                StepVerifier.create(formulaService.exportExcel())
                                .assertNext(content -> {
                                        assertEquals((byte) 'P', content[0]);
                                        assertEquals((byte) 'K', content[1]);
                                })
                                .verifyComplete();
                StepVerifier.create(formulaService.exportPdf())
                                .assertNext(content -> assertEquals("%PDF", new String(content, 0, 4, StandardCharsets.US_ASCII)))
                                .verifyComplete();
        }

        @Test
        void exportByStateFiltersBeforeCheckingMinimum() {
                List<Formula> formulas = java.util.stream.IntStream.rangeClosed(1, 19)
                                .mapToObj(index -> {
                                        Formula formula = new Formula();
                                        formula.setId(String.format("FOR%03d", index));
                                        formula.setName("Formula " + index);
                                        formula.setStandardBatch(10);
                                        formula.setUnit("kg");
                                        formula.setStatus(index <= 10);
                                        return formula;
                                })
                                .toList();
                when(formulaRepository.findAll()).thenReturn(Flux.fromIterable(formulas));

                StepVerifier.create(formulaService.exportPdf(Boolean.TRUE, "ACTIVOS"))
                                .assertNext(content -> assertEquals("%PDF", new String(content, 0, 4, StandardCharsets.US_ASCII)))
                                .verifyComplete();
                StepVerifier.create(formulaService.exportExcel(Boolean.FALSE))
                                .expectError(ResponseStatusException.class)
                                .verify();
        }

    private Formula formulaWithIngredient(String supplyId, String supplyName) {
        Formula.Ingredient ingredient = new Formula.Ingredient();
        ingredient.setSupplyId(supplyId);
        ingredient.setSupplyName(supplyName);

        Formula.Recipe recipe = new Formula.Recipe();
        recipe.setIngredients(List.of(ingredient));

        Formula formula = new Formula();
        formula.setRecipe(recipe);
        return formula;
    }
}