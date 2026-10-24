package vallegrande.edu.pe.agroTecno.service.impl;

import com.lowagie.text.DocumentException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
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
import vallegrande.edu.pe.agroTecno.model.Inventory;
import vallegrande.edu.pe.agroTecno.model.Supply;
import vallegrande.edu.pe.agroTecno.repository.SupplyRepository;
import vallegrande.edu.pe.agroTecno.service.SupplyService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class SupplyServiceImpl implements SupplyService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SupplyServiceImpl.class);
    private static final Pattern SUPPLY_ID_PATTERN = Pattern.compile("^SUP([0-9]{3,})$");
    private static final DateTimeFormatter REPORT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final TemplateEngine TEMPLATE_ENGINE = createTemplateEngine();

    private final SupplyRepository supplyRepository;

    @Autowired
    public SupplyServiceImpl(SupplyRepository supplyRepository) {
        this.supplyRepository = supplyRepository;
    }

    // ─────────────────────────────── CRUD ────────────────────────────────────

    @Override
    public Flux<Supply> findAll() {
        log.info("Listando todos los insumos / suministros");
        return supplyRepository.findAll();
    }

    @Override
    public Flux<Supply> findByStatus(Boolean status) {
        log.info("Listando insumos por estado: {}", status);
        if (status == null) {
            return Flux.empty();
        }
        return supplyRepository.findByStatus(status);
    }

    @Override
    public Flux<Supply> findByCategory(String category) {
        log.info("Listando insumos por categoría: {}", category);
        if (category == null || category.isBlank()) {
            return Flux.empty();
        }
        return supplyRepository.findByCategory(category.trim());
    }

    @Override
    public Mono<Supply> findById(String id) {
        log.info("Buscando insumo por ID: {}", id);
        return supplyRepository.findById(id);
    }

    @Override
    public Mono<Supply> save(Supply supply) {
        log.info("Registrando insumo: {}", supply);
        if (supply == null) {
            return Mono.error(badRequest("El insumo es requerido"));
        }

        Mono<String> idMono = (supply.getId() != null && !supply.getId().isBlank())
                ? supplyRepository.existsById(supply.getId())
                        .flatMap(exists -> exists
                                ? Mono.error(badRequest("El insumo con ID " + supply.getId() + " ya existe en la base de datos"))
                                : Mono.just(supply.getId()))
                : nextSupplyId();

        return idMono.flatMap(id -> {
            supply.setId(id);
            if (supply.getCategory() == null || supply.getCategory().isBlank()) {
                supply.setCategory("General");
            }
            validate(supply);
            if (supply.getStatus() == null) {
                supply.setStatus(true);
            }
            if (supply.getCreatedAt() == null) {
                supply.setCreatedAt(LocalDateTime.now());
            }
            if (supply.getUpdateAt() == null) {
                supply.setUpdateAt(LocalDateTime.now());
            }
            return supplyRepository.save(supply);
        });
    }

    private Mono<String> nextSupplyId() {
        return supplyRepository.findAll()
                .map(Supply::getId)
                .filter(id -> id != null && SUPPLY_ID_PATTERN.matcher(id).matches())
                .map(id -> new BigInteger(id.substring(3)))
                .reduce(BigInteger.ZERO, BigInteger::max)
                .map(maxId -> String.format(Locale.ROOT, "SUP%03d", maxId.add(BigInteger.ONE)));
    }

    @Override
    public Mono<Supply> update(String id, Supply supply) {
        log.info("Actualizando insumo con ID: {}", id);
        return supplyRepository.findById(id)
                .flatMap(existing -> {
                    supply.setId(id);
                    if (supply.getCategory() == null || supply.getCategory().isBlank()) {
                        supply.setCategory(existing.getCategory() != null ? existing.getCategory() : "General");
                    }
                    validate(supply);
                    existing.setName(supply.getName());
                    existing.setDescription(supply.getDescription());
                    existing.setCategory(supply.getCategory());
                    existing.setInventory(supply.getInventory());
                    existing.setUnit(supply.getUnit());
                    existing.setLocation(supply.getLocation());
                    if (supply.getStatus() != null) {
                        existing.setStatus(supply.getStatus());
                    }
                    existing.setUpdateAt(LocalDateTime.now());
                    return supplyRepository.save(existing);
                });
    }

    @Override
    public Mono<Supply> delete(String id) {
        log.info("Eliminando (lógico) insumo con ID: {}", id);
        return supplyRepository.findById(id)
                .flatMap(existing -> {
                    existing.setStatus(false);
                    existing.setDeletedAt(LocalDateTime.now());
                    existing.setUpdateAt(LocalDateTime.now());
                    return supplyRepository.save(existing);
                });
    }

    @Override
    public Mono<Supply> restore(String id) {
        log.info("Restaurando insumo con ID: {}", id);
        return supplyRepository.findById(id)
                .flatMap(existing -> {
                    existing.setStatus(true);
                    existing.setRestoredAt(LocalDateTime.now());
                    existing.setUpdateAt(LocalDateTime.now());
                    return supplyRepository.save(existing);
                });
    }

    // ─────────────────────────────── IMPORTACIÓN ─────────────────────────────

    @Override
    public Mono<List<Supply>> importCsv(byte[] content) {
        log.info("Importando insumos desde CSV");
        return Mono.fromCallable(() -> parseCsv(content))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(supplies -> Flux.fromIterable(supplies)
                        .concatMap(this::save)
                        .collectList());
    }

    @Override
    public Mono<List<Supply>> importExcel(byte[] content) {
        log.info("Importando insumos desde Excel");
        return Mono.fromCallable(() -> parseExcel(content))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(supplies -> Flux.fromIterable(supplies)
                        .concatMap(this::save)
                        .collectList());
    }

    // ─────────────────────────────── EXPORTACIÓN ─────────────────────────────

    @Override
    public Mono<byte[]> exportExcel() {
        return exportExcel(null);
    }

    @Override
    public Mono<byte[]> exportExcel(Boolean status) {
        log.info("Exportando insumos a Excel, estado={}", status);
        return suppliesForExport(status).flatMap(supplies ->
                Mono.fromCallable(() -> createExcel(supplies))
                        .subscribeOn(Schedulers.boundedElastic()));
    }

    @Override
    public Mono<byte[]> exportPdf() {
        return exportPdf(null, "TODOS");
    }

    @Override
    public Mono<byte[]> exportPdf(Boolean status, String estadoReporte) {
        log.info("Exportando insumos a PDF, estado={}", status);
        return suppliesForExport(status).flatMap(supplies ->
                Mono.fromCallable(() -> createPdf(supplies, estadoReporte))
                        .subscribeOn(Schedulers.boundedElastic()));
    }

    // ─────────────────────────── PARSERS PRIVADOS ────────────────────────────

    private List<Supply> parseCsv(byte[] content) throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .build();

        List<Supply> supplies = new ArrayList<>();

        try (CSVParser parser = CSVParser.parse(
                new ByteArrayInputStream(content), StandardCharsets.UTF_8, format)) {

            List<String> headers = parser.getHeaderNames().stream()
                    .map(h -> h.toLowerCase(Locale.ROOT))
                    .toList();

            if (!headers.containsAll(List.of("name", "unit", "location",
                    "current_stock", "minimum_stock", "expiration_date"))) {
                throw badRequest("El CSV requiere al menos las columnas: name, unit, location, "
                        + "current_stock, minimum_stock, expiration_date");
            }

            for (CSVRecord record : parser) {
                Supply supply = buildSupplyFromCsvRecord(record);
                supplies.add(supply);
            }
        }

        if (supplies.size() < 10) {
            throw badRequest("La importación requiere al menos 10 registros; recibidos: " + supplies.size());
        }
        return supplies;
    }

    private Supply buildSupplyFromCsvRecord(CSVRecord record) {
        Supply supply = new Supply();

        String id = csvValue(record, "id");
        if (id != null && !id.isBlank()) {
            if (!SUPPLY_ID_PATTERN.matcher(id).matches()) {
                throw badRequest("El campo 'id' debe cumplir el patrón ^SUP[0-9]{3,}$; valor recibido: " + id);
            }
            supply.setId(id);
        } else {
            supply.setId(null);
        }

        supply.setName(csvValue(record, "name"));
        supply.setDescription(csvValue(record, "description"));

        String category = csvValue(record, "category", "categoría", "categoria");
        supply.setCategory(category != null && !category.isBlank() ? category : "General");

        supply.setUnit(csvValue(record, "unit"));
        supply.setLocation(csvValue(record, "location"));

        Inventory inv = new Inventory();
        inv.setCurrent_stock(csvDoubleValue(record, "current_stock"));
        inv.setMinimum_stock(csvDoubleValue(record, "minimum_stock"));
        inv.setExpiration_date(csvDateValue(record, "expiration_date"));
        supply.setInventory(inv);

        if (supply.getName() == null || supply.getName().isBlank()
                || supply.getUnit() == null || supply.getUnit().isBlank()
                || supply.getLocation() == null || supply.getLocation().isBlank()
                || inv.getCurrent_stock() == null
                || inv.getMinimum_stock() == null
                || inv.getExpiration_date() == null) {
            throw badRequest("Cada fila requiere: name, unit, location, current_stock, minimum_stock, expiration_date");
        }

        return supply;
    }

    private List<Supply> parseExcel(byte[] content) throws IOException {
        List<Supply> supplies = new ArrayList<>();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw badRequest("El archivo Excel no contiene hojas");
            }

            Iterator<Row> rows = sheet.rowIterator();
            if (!rows.hasNext()) {
                throw badRequest("El archivo Excel está vacío");
            }

            Row headerRow = rows.next();
            int idCol = -1, nameCol = -1, descCol = -1, catCol = -1, unitCol = -1,
                    locationCol = -1, curStockCol = -1, minStockCol = -1, expDateCol = -1;

            for (Cell cell : headerRow) {
                String colName = cellStringValue(cell).toLowerCase(Locale.ROOT);
                switch (colName) {
                    case "id" -> idCol = cell.getColumnIndex();
                    case "name", "nombre" -> nameCol = cell.getColumnIndex();
                    case "description", "descripcion", "descripción" -> descCol = cell.getColumnIndex();
                    case "category", "categoria", "categoría" -> catCol = cell.getColumnIndex();
                    case "unit", "unidad" -> unitCol = cell.getColumnIndex();
                    case "location", "ubicacion", "ubicación" -> locationCol = cell.getColumnIndex();
                    case "current_stock", "stock_actual" -> curStockCol = cell.getColumnIndex();
                    case "minimum_stock", "stock_minimo" -> minStockCol = cell.getColumnIndex();
                    case "expiration_date", "fecha_vencimiento" -> expDateCol = cell.getColumnIndex();
                    default -> { /* ignorar columnas extra */ }
                }
            }

            if (nameCol < 0 || unitCol < 0 || locationCol < 0
                    || curStockCol < 0 || minStockCol < 0 || expDateCol < 0) {
                throw badRequest("El Excel requiere las columnas: name, unit, location, "
                        + "current_stock, minimum_stock, expiration_date");
            }

            while (rows.hasNext()) {
                Row row = rows.next();
                if (isRowEmpty(row)) continue;

                Supply supply = new Supply();

                if (idCol >= 0) {
                    String id = excelStringValue(row, idCol);
                    if (id != null && !id.isBlank()) {
                        if (!SUPPLY_ID_PATTERN.matcher(id).matches()) {
                            throw badRequest("Fila " + (row.getRowNum() + 1)
                                    + ": 'id' debe cumplir el patrón ^SUP[0-9]{3,}$; valor: " + id);
                        }
                        supply.setId(id);
                    } else {
                        supply.setId(null);
                    }
                } else {
                    supply.setId(null);
                }

                supply.setName(excelStringValue(row, nameCol));
                supply.setDescription(descCol >= 0 ? excelStringValue(row, descCol) : null);
                supply.setCategory(catCol >= 0 && excelStringValue(row, catCol) != null
                        ? excelStringValue(row, catCol) : "General");
                supply.setUnit(excelStringValue(row, unitCol));
                supply.setLocation(excelStringValue(row, locationCol));

                Inventory inv = new Inventory();
                inv.setCurrent_stock(excelDoubleValue(row, curStockCol, "current_stock"));
                inv.setMinimum_stock(excelDoubleValue(row, minStockCol, "minimum_stock"));
                inv.setExpiration_date(excelDateValue(row, expDateCol));
                supply.setInventory(inv);

                if (supply.getName() == null || supply.getName().isBlank()
                        || supply.getUnit() == null || supply.getUnit().isBlank()
                        || supply.getLocation() == null || supply.getLocation().isBlank()
                        || inv.getCurrent_stock() == null
                        || inv.getMinimum_stock() == null
                        || inv.getExpiration_date() == null) {
                    throw badRequest("Fila " + (row.getRowNum() + 1)
                            + ": campos obligatorios incompletos");
                }

                supplies.add(supply);
            }
        }

        if (supplies.size() < 10) {
            throw badRequest("La importación requiere al menos 10 registros; recibidos: " + supplies.size());
        }
        return supplies;
    }

    // ──────────────────────────── GENERADORES ────────────────────────────────

    private byte[] createExcel(List<Supply> supplies) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Insumos");
            String[] headers = {"id", "name", "description", "category", "unit", "location",
                    "current_stock", "minimum_stock", "expiration_date",
                    "status", "created_at", "update_at", "deleted_at", "restored_at"};

            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < headers.length; col++) {
                headerRow.createCell(col).setCellValue(headers[col]);
            }

            for (int i = 0; i < supplies.size(); i++) {
                Supply supply = supplies.get(i);
                Row row = sheet.createRow(i + 1);
                Inventory inv = supply.getInventory();

                Object[] values = {
                        supply.getId(),
                        supply.getName(),
                        supply.getDescription(),
                        supply.getCategory(),
                        supply.getUnit(),
                        supply.getLocation(),
                        inv != null ? inv.getCurrent_stock() : null,
                        inv != null ? inv.getMinimum_stock() : null,
                        inv != null && inv.getExpiration_date() != null ? inv.getExpiration_date().toString() : null,
                        supply.getStatus(),
                        supply.getCreatedAt() != null ? supply.getCreatedAt().toString() : null,
                        supply.getUpdateAt() != null ? supply.getUpdateAt().toString() : null,
                        supply.getDeletedAt() != null ? supply.getDeletedAt().toString() : null,
                        supply.getRestoredAt() != null ? supply.getRestoredAt().toString() : null
                };

                for (int col = 0; col < values.length; col++) {
                    if (values[col] != null) {
                        row.createCell(col).setCellValue(values[col].toString());
                    }
                }
            }

            for (int col = 0; col < headers.length; col++) {
                sheet.autoSizeColumn(col);
            }

            workbook.write(output);
            return output.toByteArray();
        }
    }

    private byte[] createPdf(List<Supply> supplies, String estadoReporte) throws IOException {
        Context context = new Context(Locale.forLanguageTag("es-PE"));
        context.setVariable("supplies", supplies);
        context.setVariable("insumos", supplies);
        context.setVariable("estadoReporte", estadoReporte);
        context.setVariable("fechaGeneracion", LocalDateTime.now().format(REPORT_DATE_FORMAT));
        String html = TEMPLATE_ENGINE.process("supply", context);

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(output);
            return output.toByteArray();
        } catch (DocumentException e) {
            throw new IOException("Error generando PDF: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────── HELPERS CSV ─────────────────────────────────

    private String csvValue(CSVRecord record, String... columnAliases) {
        for (String alias : columnAliases) {
            String actual = record.getParser().getHeaderNames().stream()
                    .filter(h -> h.equalsIgnoreCase(alias))
                    .findFirst()
                    .orElse(null);
            if (actual != null && record.isSet(actual) && !record.get(actual).isBlank()) {
                return record.get(actual).trim();
            }
        }
        return null;
    }

    private Double csvDoubleValue(CSVRecord record, String column) {
        String value = csvValue(record, column);
        try {
            return value == null ? null : Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw badRequest("Valor numérico inválido para '" + column + "': " + value);
        }
    }

    private LocalDate csvDateValue(CSVRecord record, String column) {
        String value = csvValue(record, column);
        try {
            return value == null ? null : LocalDate.parse(value);
        } catch (Exception e) {
            throw badRequest("Fecha inválida para '" + column + "' (use YYYY-MM-DD): " + value);
        }
    }

    // ─────────────────────────── HELPERS EXCEL ───────────────────────────────

    private String excelStringValue(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        String val = cellStringValue(cell);
        return val.isBlank() ? null : val;
    }

    private Double excelDoubleValue(Row row, int col, String colName) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        String val = cellStringValue(cell);
        try {
            return val.isBlank() ? null : Double.parseDouble(val);
        } catch (NumberFormatException e) {
            throw badRequest("Valor numérico inválido para '" + colName + "': " + val);
        }
    }

    private LocalDate excelDateValue(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }
        String val = cellStringValue(cell);
        try {
            return val.isBlank() ? null : LocalDate.parse(val);
        } catch (Exception e) {
            throw badRequest("Fecha inválida en columna expiration_date (use YYYY-MM-DD o formato fecha Excel): " + val);
        }
    }

    private String cellStringValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double d = cell.getNumericCellValue();
                yield d == Math.floor(d) ? String.valueOf((long) d) : String.valueOf(d);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK
                    && !cellStringValue(cell).isBlank()) {
                return false;
            }
        }
        return true;
    }

    // ─────────────────────────── UTILIDADES ──────────────────────────────────

    private Mono<List<Supply>> suppliesForExport(Boolean status) {
        return supplyRepository.findAll()
                .filter(supply -> status == null || status.equals(supply.getStatus()))
                .collectList()
                .flatMap(list -> list.size() < 10
                        ? Mono.error(badRequest("La exportación requiere al menos 10 registros; encontrados: " + list.size()))
                        : Mono.just(list));
    }

    private void validate(Supply supply) {
        if (supply.getId() == null || !SUPPLY_ID_PATTERN.matcher(supply.getId()).matches()) {
            throw badRequest("id must match ^SUP[0-9]{3,}$");
        }
        if (supply.getName() == null || supply.getName().isBlank()
                || supply.getCategory() == null || supply.getCategory().isBlank()
                || supply.getInventory() == null
                || supply.getInventory().getCurrent_stock() == null
                || supply.getInventory().getMinimum_stock() == null
                || supply.getInventory().getExpiration_date() == null
                || supply.getUnit() == null || supply.getUnit().isBlank()
                || supply.getLocation() == null || supply.getLocation().isBlank()) {
            throw badRequest("name, category, inventory, unit and location are required");
        }
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
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
}
