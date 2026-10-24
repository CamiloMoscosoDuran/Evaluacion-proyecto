package vallegrande.edu.pe.agroTecno.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.DocumentException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import vallegrande.edu.pe.agroTecno.model.Formula;
import vallegrande.edu.pe.agroTecno.repository.FormulaRepository;
import vallegrande.edu.pe.agroTecno.repository.SupplyRepository;
import vallegrande.edu.pe.agroTecno.service.FormulaService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class FormulaServiceImpl implements FormulaService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FormulaServiceImpl.class);
    private static final java.util.regex.Pattern FORMULA_ID_PATTERN = java.util.regex.Pattern.compile("^FOR([0-9]{3,})$");
    private static final DateTimeFormatter REPORT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final TemplateEngine TEMPLATE_ENGINE = createTemplateEngine();

    private final FormulaRepository formulaRepository;
    private final SupplyRepository supplyRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public FormulaServiceImpl(FormulaRepository formulaRepository, SupplyRepository supplyRepository,
                              ObjectMapper objectMapper) {
        this.formulaRepository = formulaRepository;
        this.supplyRepository = supplyRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Flux<Formula> findAll() {
        log.info("Listando todas las formulas");
        return formulaRepository.findAll();
    }

    @Override
    public Flux<Formula> findByEstado(String estado) {
        log.info("Listando formulas por estado: {}", estado);
        if (estado == null) {
            return Flux.empty();
        }

        String normalized = estado.trim().toUpperCase();
        Boolean status = switch (normalized) {
            case "A", "ACTIVO", "TRUE", "1" -> Boolean.TRUE;
            case "I", "INACTIVO", "FALSE", "0" -> Boolean.FALSE;
            default -> null;
        };

        if (status == null) {
            return Flux.empty();
        }

        return formulaRepository.findByStatus(status);
    }

    @Override
    public Mono<Formula> findById(String id) {
        log.info("Buscando formula por ID: {}", id);
        return formulaRepository.findById(id);
    }

    @Override
    public Mono<Formula> save(Formula formula) {
        log.info("Registrando formula: {}", formula);
        if (formula == null) {
            return Mono.error(new IllegalArgumentException("Formula request is required"));
        }
        return nextFormulaId()
                .flatMap(generatedId -> {
                    formula.setId(generatedId);
                    return resolveIngredientNames(formula);
                })
                .flatMap(validatedFormula -> {
                    if (validatedFormula.getStatus() == null) {
                        validatedFormula.setStatus(Boolean.TRUE);
                    }
                    if (validatedFormula.getCreatedAt() == null) {
                        validatedFormula.setCreatedAt(LocalDateTime.now());
                    }
                    if (validatedFormula.getUpdateAt() == null) {
                        validatedFormula.setUpdateAt(LocalDateTime.now());
                    }
                    return formulaRepository.save(validatedFormula);
                });
    }

    private Mono<String> nextFormulaId() {
        return formulaRepository.findAll()
                .map(Formula::getId)
                .filter(id -> id != null && FORMULA_ID_PATTERN.matcher(id).matches())
                .map(id -> new java.math.BigInteger(id.substring(3)))
                .reduce(java.math.BigInteger.ZERO, java.math.BigInteger::max)
                .map(maxId -> String.format(java.util.Locale.ROOT, "FOR%03d", maxId.add(java.math.BigInteger.ONE)));
    }

    @Override
    public Mono<Formula> update(String id, Formula formula) {
        log.info("Actualizando formula con ID: {}", id);
        return formulaRepository.findById(id)
                .flatMap(existing -> resolveIngredientNames(formula)
                        .flatMap(validatedFormula -> {
                            existing.setName(validatedFormula.getName());
                            existing.setDescription(validatedFormula.getDescription());
                            existing.setStandardBatch(validatedFormula.getStandardBatch());
                            existing.setUnit(validatedFormula.getUnit());
                            existing.setProductionTime(validatedFormula.getProductionTime());
                            existing.setPreparationCost(validatedFormula.getPreparationCost());
                            existing.setSuggestedPrice(validatedFormula.getSuggestedPrice());
                            existing.setRecipe(validatedFormula.getRecipe());
                            existing.setFormulaInventory(validatedFormula.getFormulaInventory());
                            if (validatedFormula.getStatus() != null) {
                                existing.setStatus(validatedFormula.getStatus());
                            }
                            existing.setUpdateAt(LocalDateTime.now());
                            return formulaRepository.save(existing);
                        }));
    }

    @Override
    public Mono<Formula> delete(String id) {
        log.info("Eliminando (logico) formula con ID: {}", id);
        return formulaRepository.findById(id)
                .flatMap(existing -> {
                    existing.setStatus(Boolean.FALSE);
                    existing.setDeletedAt(LocalDateTime.now());
                    return formulaRepository.save(existing);
                });
    }

    private Mono<Formula> resolveIngredientNames(Formula formula) {
        if (formula == null) {
            return Mono.error(new IllegalArgumentException("Formula request is required"));
        }
        if (formula.getRecipe() == null || formula.getRecipe().getIngredients() == null
                || formula.getRecipe().getIngredients().isEmpty()) {
            return Mono.just(formula);
        }

        return Flux.fromIterable(formula.getRecipe().getIngredients())
                .concatMap(ingredient -> {
                    if (ingredient == null || ingredient.getSupplyId() == null
                            || ingredient.getSupplyId().isBlank()) {
                        return Mono.error(new IllegalArgumentException("Each recipe ingredient must have a supply_id"));
                    }
                    String supplyId = ingredient.getSupplyId().trim();
                    return supplyRepository.findById(supplyId)
                            .switchIfEmpty(Mono.error(new IllegalArgumentException(
                                    "Supply not found in MongoDB with ID: " + supplyId)))
                            .map(supply -> {
                                ingredient.setSupplyId(supply.getId());
                                ingredient.setSupplyName(supply.getName());
                                return ingredient;
                            });
                })
                .collectList()
                .map(ingredients -> {
                    formula.getRecipe().setIngredients(ingredients);
                    return formula;
                });
    }

    @Override
    public Mono<Formula> restore(String id) {
        log.info("Restaurando formula con ID: {}", id);
        return formulaRepository.findById(id)
                .flatMap(existing -> {
                    existing.setStatus(Boolean.TRUE);
                    existing.setRestoredAt(LocalDateTime.now());
                    return formulaRepository.save(existing);
                });
    }

    @Override
    public Mono<List<Formula>> importCsv(byte[] content) {
        return Mono.fromCallable(() -> parseCsv(content))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(formulas -> Flux.fromIterable(formulas)
                        .concatMap(this::save)
                        .collectList());
    }

    private List<Formula> parseCsv(byte[] content) throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .build();
        List<Formula> formulas = new ArrayList<>();
        try (CSVParser parser = CSVParser.parse(new ByteArrayInputStream(content), StandardCharsets.UTF_8, format)) {
            List<String> headers = parser.getHeaderNames().stream()
                    .map(header -> header.toLowerCase(Locale.ROOT))
                    .toList();
            if (!headers.containsAll(List.of("name", "standard_batch", "unit"))) {
                throw badRequest("El CSV requiere las columnas name, standard_batch y unit");
            }
            for (CSVRecord record : parser) {
                Formula formula = new Formula();
                formula.setName(value(record, "name"));
                formula.setDescription(value(record, "description"));
                formula.setStandardBatch(integerValue(record, "standard_batch"));
                formula.setUnit(value(record, "unit"));
                formula.setProductionTime(integerValue(record, "production_time"));
                formula.setPreparationCost(decimalValue(record, "preparation_cost"));
                formula.setSuggestedPrice(decimalValue(record, "suggested_price"));
                formula.setStatus(booleanValue(record, "status"));
                formula.setRecipe(jsonValue(record, "recipe", Formula.Recipe.class));
                formula.setFormulaInventory(jsonFormulaInventory(record));
                if (formula.getName() == null || formula.getName().isBlank()
                        || formula.getStandardBatch() == null || formula.getStandardBatch() <= 0
                        || formula.getUnit() == null || formula.getUnit().isBlank()) {
                    throw badRequest("Cada fila requiere name, standard_batch mayor que cero y unit");
                }
                if (formula.getRecipe() == null || formula.getRecipe().getVersion() == null
                        || formula.getRecipe().getVersion().isBlank()
                        || formula.getRecipe().getIngredients() == null) {
                    throw badRequest("Cada fila requiere recipe con version e ingredients");
                }
                if (formula.getRecipe().getIngredients().stream().anyMatch(ingredient -> ingredient == null
                        || ingredient.getSupplyId() == null || ingredient.getSupplyId().isBlank()
                        || ingredient.getQuantityRequired() == null || ingredient.getPercentage() == null)) {
                    throw badRequest("Cada ingrediente requiere supply_id, quantity_required y percentage");
                }
                formulas.add(formula);
            }
        }
        if (formulas.size() < 10) {
            throw badRequest("La importación requiere al menos 10 registros; recibidos: " + formulas.size());
        }
        return formulas;
    }

    private String value(CSVRecord record, String column) {
        String actualHeader = record.getParser().getHeaderNames().stream()
            .filter(header -> header.equalsIgnoreCase(column))
            .findFirst()
            .orElse(null);
        return actualHeader != null && record.isSet(actualHeader) && !record.get(actualHeader).isBlank()
            ? record.get(actualHeader) : null;
    }

    private Integer integerValue(CSVRecord record, String column) {
        String value = value(record, column);
        try {
            return value == null ? null : Integer.valueOf(value);
        } catch (NumberFormatException exception) {
            throw badRequest("Valor inválido para " + column + ": " + value);
        }
    }

    private BigDecimal decimalValue(CSVRecord record, String column) {
        String value = value(record, column);
        try {
            return value == null ? null : new BigDecimal(value);
        } catch (NumberFormatException exception) {
            throw badRequest("Valor inválido para " + column + ": " + value);
        }
    }

    private Boolean booleanValue(CSVRecord record, String column) {
        String value = value(record, column);
        if (value == null) {
            return null;
        }
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "true", "1", "a", "activo" -> true;
            case "false", "0", "i", "inactivo" -> false;
            default -> throw badRequest("Valor inválido para " + column + ": " + value);
        };
    }

    private <T> T jsonValue(CSVRecord record, String column, Class<T> type) {
        String value = value(record, column);
        try {
            return value == null ? null : objectMapper.readValue(value, type);
        } catch (IOException exception) {
            throw badRequest("JSON inválido en la columna " + column);
        }
    }

    private List<Formula.FormulaInventory> jsonFormulaInventory(CSVRecord record) {
        String value = value(record, "formula_inventory");
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(value, new com.fasterxml.jackson.core.type.TypeReference<List<Formula.FormulaInventory>>() {});
        } catch (IOException exception) {
            try {
                Formula.FormulaInventory single = objectMapper.readValue(value, Formula.FormulaInventory.class);
                return single != null ? List.of(single) : null;
            } catch (IOException ex) {
                throw badRequest("JSON inválido en la columna formula_inventory");
            }
        }
    }

    @Override
    public Mono<byte[]> exportExcel() {
        return exportExcel(null);
    }

    @Override
    public Mono<byte[]> exportExcel(Boolean status) {
        return formulasForExport(status).flatMap(formulas -> Mono.fromCallable(() -> createExcel(formulas))
                .subscribeOn(Schedulers.boundedElastic()));
    }

    private byte[] createExcel(List<Formula> formulas) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Fórmulas");
            String[] headers = {"id", "name", "description", "standard_batch", "unit", "production_time",
                    "preparation_cost", "suggested_price", "status", "recipe", "formula_inventory",
                    "created_at", "update_at", "deleted_at", "restored_at"};
            Row header = sheet.createRow(0);
            for (int column = 0; column < headers.length; column++) {
                header.createCell(column).setCellValue(headers[column]);
            }
            for (int index = 0; index < formulas.size(); index++) {
                Formula formula = formulas.get(index);
                Row row = sheet.createRow(index + 1);
                Object[] values = {formula.getId(), formula.getName(), formula.getDescription(),
                        formula.getStandardBatch(), formula.getUnit(), formula.getProductionTime(),
                        formula.getPreparationCost(), formula.getSuggestedPrice(), formula.getStatus(),
                        json(formula.getRecipe()), json(formula.getFormulaInventory()), formula.getCreatedAt(),
                        formula.getUpdateAt(), formula.getDeletedAt(), formula.getRestoredAt()};
                for (int column = 0; column < values.length; column++) {
                    if (values[column] != null) {
                        row.createCell(column).setCellValue(values[column].toString());
                    }
                }
            }
            for (int column = 0; column < headers.length; column++) {
                sheet.autoSizeColumn(column);
            }
            workbook.write(output);
            return output.toByteArray();
        }
    }

    private String json(Object value) throws IOException {
        return value == null ? null : objectMapper.writeValueAsString(value);
    }

    @Override
    public Mono<byte[]> exportPdf() {
        return exportPdf(null, "TODOS");
    }

    @Override
    public Mono<byte[]> exportPdf(Boolean status, String estadoReporte) {
        return formulasForExport(status).flatMap(formulas -> Mono.fromCallable(() -> createPdf(formulas, estadoReporte))
                .subscribeOn(Schedulers.boundedElastic()));
    }

    private byte[] createPdf(List<Formula> formulas, String estadoReporte) throws IOException {
        Context context = new Context(Locale.forLanguageTag("es-PE"));
        context.setVariable("formulas", formulas);
        context.setVariable("estadoReporte", estadoReporte);
        context.setVariable("fechaGeneracion", LocalDateTime.now().format(REPORT_DATE_FORMAT));
        String html = TEMPLATE_ENGINE.process("formula", context);

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(output);
            return output.toByteArray();
        }
    }

    private static TemplateEngine createTemplateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resolver.setCacheable(true);

        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }

    private Mono<List<Formula>> formulasForExport(Boolean status) {
        return formulaRepository.findAll()
                .filter(formula -> status == null || status.equals(formula.getStatus()))
                .collectList()
                .flatMap(formulas -> formulas.size() < 10
                ? Mono.error(badRequest("La exportación requiere al menos 10 registros para el estado seleccionado; encontrados: " + formulas.size()))
                : Mono.just(formulas));
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

}