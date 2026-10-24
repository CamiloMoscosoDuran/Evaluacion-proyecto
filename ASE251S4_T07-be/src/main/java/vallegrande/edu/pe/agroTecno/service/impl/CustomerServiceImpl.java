package vallegrande.edu.pe.agroTecno.service.impl;

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
import vallegrande.edu.pe.agroTecno.model.Customer;
import vallegrande.edu.pe.agroTecno.repository.CustomerRepository;
import vallegrande.edu.pe.agroTecno.service.CustomerService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CustomerServiceImpl implements CustomerService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CustomerServiceImpl.class);
    private static final Pattern CUSTOMER_ID_PATTERN = Pattern.compile("^(?:CUS|CLI)([0-9]{3,})$");
    private static final DateTimeFormatter REPORT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final TemplateEngine TEMPLATE_ENGINE = createTemplateEngine();

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public Flux<Customer> findAll() {
        log.info("Listando todos los clientes");
        return customerRepository.findAll();
    }

    @Override
    public Flux<Customer> findByStatus(Boolean status) {
        log.info("Listando clientes por status: {}", status);
        if (status == null) {
            return Flux.empty();
        }
        return customerRepository.findByStatus(status);
    }

    @Override
    public Flux<Customer> findByEstado(String estado) {
        log.info("Listando clientes por estado: {}", estado);
        if (estado == null || estado.isBlank()) {
            return Flux.empty();
        }
        Boolean status = parseStatusToBoolean(estado);
        return customerRepository.findByStatus(status);
    }

    @Override
    public Mono<Customer> findById(String id) {
        log.info("Buscando cliente por ID: {}", id);
        return customerRepository.findById(id);
    }

    @Override
    public Mono<Customer> save(Customer customer) {
        log.info("Registrando cliente: {}", customer);
        if (customer == null) {
            return Mono.error(badRequest("El cliente es requerido"));
        }

        Mono<String> idMono = (customer.getId() != null && !customer.getId().isBlank())
                ? Mono.just(customer.getId().trim().toUpperCase(Locale.ROOT))
                : nextCustomerId();

        return idMono.flatMap(id -> {
            customer.setId(id);
            if (customer.getStatus() == null) {
                customer.setStatus(true);
            }
            if (customer.getCreatedAt() == null) {
                customer.setCreatedAt(LocalDateTime.now());
            }
            if (customer.getUpdateAt() == null) {
                customer.setUpdateAt(LocalDateTime.now());
            }
            if (customer.getRegistrationDate() == null) {
                customer.setRegistrationDate(LocalDate.now());
            }
            return customerRepository.save(customer);
        });
    }

    private Mono<String> nextCustomerId() {
        return customerRepository.findAll()
                .map(Customer::getId)
                .filter(id -> id != null && CUSTOMER_ID_PATTERN.matcher(id).matches())
                .map(id -> {
                    Matcher matcher = CUSTOMER_ID_PATTERN.matcher(id);
                    if (matcher.matches()) {
                        return new BigInteger(matcher.group(1));
                    }
                    return BigInteger.ZERO;
                })
                .reduce(BigInteger.ZERO, BigInteger::max)
                .map(maxId -> String.format(Locale.ROOT, "CUS%03d", maxId.add(BigInteger.ONE)));
    }

    @Override
    public Mono<Customer> update(String id, Customer customer) {
        log.info("Actualizando cliente con ID: {}", id);
        return customerRepository.findById(id)
                .flatMap(existing -> {
                    existing.setFirstName(customer.getFirstName());
                    existing.setLastName(customer.getLastName());
                    existing.setDocumentType(customer.getDocumentType());
                    existing.setDocumentNumber(customer.getDocumentNumber());
                    if (customer.getPhone() != null) {
                        existing.setPhone(customer.getPhone());
                    }
                    existing.setEmail(customer.getEmail());
                    if (customer.getRegistrationDate() != null) {
                        existing.setRegistrationDate(customer.getRegistrationDate());
                    }
                    if (customer.getStatus() != null) {
                        existing.setStatus(customer.getStatus());
                    }
                    if (customer.getFarmPlot() != null) {
                        existing.setFarmPlot(customer.getFarmPlot());
                    }
                    if (customer.getAddress() != null) {
                        existing.setAddress(customer.getAddress());
                    }
                    existing.setUpdateAt(LocalDateTime.now());
                    return customerRepository.save(existing);
                });
    }

    @Override
    public Mono<Customer> delete(String id) {
        log.info("Eliminando (lógico) cliente con ID: {}", id);
        return customerRepository.findById(id)
                .flatMap(existing -> {
                    existing.setStatus(false);
                    existing.setDeletedAt(LocalDateTime.now());
                    existing.setUpdateAt(LocalDateTime.now());
                    return customerRepository.save(existing);
                });
    }

    @Override
    public Mono<Customer> restore(String id) {
        log.info("Restaurando cliente con ID: {}", id);
        return customerRepository.findById(id)
                .flatMap(existing -> {
                    existing.setStatus(true);
                    existing.setRestoredAt(LocalDateTime.now());
                    existing.setUpdateAt(LocalDateTime.now());
                    return customerRepository.save(existing);
                });
    }

    // ─────────────────────────────── IMPORTACIÓN CSV ─────────────────────────

    @Override
    public Mono<List<Customer>> importCsv(byte[] content) {
        log.info("Importando clientes desde archivo CSV");
        return Mono.fromCallable(() -> parseCsv(content))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(customers -> Flux.fromIterable(customers)
                        .concatMap(this::save)
                        .collectList());
    }

    private List<Customer> parseCsv(byte[] content) throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .build();

        List<Customer> customers = new ArrayList<>();
        try (CSVParser parser = CSVParser.parse(new ByteArrayInputStream(content), StandardCharsets.UTF_8, format)) {
            List<String> headers = parser.getHeaderNames().stream()
                    .map(h -> h.trim().toLowerCase(Locale.ROOT))
                    .toList();

            boolean hasFirstName = headers.stream().anyMatch(h -> h.contains("first_name") || h.contains("nombre"));
            boolean hasLastName = headers.stream().anyMatch(h -> h.contains("last_name") || h.contains("apellido"));
            boolean hasDocNum = headers.stream().anyMatch(h -> h.contains("document_number") || h.contains("nro") || h.contains("doc"));

            if (!hasFirstName || !hasLastName || !hasDocNum) {
                throw badRequest("El CSV requiere las columnas first_name, last_name y document_number");
            }

            for (CSVRecord record : parser) {
                Customer customer = new Customer();

                String idVal = value(record, "id", "código", "codigo");
                if (idVal != null && CUSTOMER_ID_PATTERN.matcher(idVal).matches()) {
                    customer.setId(idVal);
                }

                customer.setFirstName(value(record, "first_name", "nombre", "nombres"));
                customer.setLastName(value(record, "last_name", "apellido", "apellidos"));
                customer.setDocumentType(value(record, "document_type", "tipo doc.", "tipo_doc", "tipo_documento"));
                customer.setDocumentNumber(value(record, "document_number", "nro. doc", "nro_doc", "numero_documento"));

                String phoneVal = value(record, "phone", "teléfono", "telefono", "celular");
                if (phoneVal != null && !phoneVal.isBlank()) {
                    customer.setPhone(parsePhoneList(phoneVal));
                } else {
                    customer.setPhone(Collections.emptyList());
                }

                customer.setEmail(value(record, "email", "correo", "correo_electronico"));

                String regDateStr = value(record, "registration_date", "fecha reg", "fecha_reg", "fecha_registro");
                if (regDateStr != null) {
                    try {
                        customer.setRegistrationDate(LocalDate.parse(regDateStr));
                    } catch (Exception e) {
                        throw badRequest("Formato de fecha inválido para registration_date (use YYYY-MM-DD): " + regDateStr);
                    }
                } else {
                    customer.setRegistrationDate(LocalDate.now());
                }

                String statusStr = value(record, "status", "estado");
                customer.setStatus(parseStatusToBoolean(statusStr));

                if (customer.getFirstName() == null || customer.getFirstName().isBlank()
                        || customer.getLastName() == null || customer.getLastName().isBlank()
                        || customer.getDocumentNumber() == null || customer.getDocumentNumber().isBlank()) {
                    throw badRequest("Cada fila requiere first_name, last_name y document_number");
                }

                if (customer.getDocumentType() == null || customer.getDocumentType().isBlank()) {
                    customer.setDocumentType("DNI");
                }

                customers.add(customer);
            }
        }

        if (customers.size() < 10) {
            throw badRequest("La importación requiere al menos 10 registros; recibidos: " + customers.size());
        }
        return customers;
    }

    private List<String> parsePhoneList(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }
        String clean = value.replace("[", "").replace("]", "").replace("\"", "").replace("'", "");
        String[] parts = clean.split("[,;|/]");
        return Arrays.stream(parts)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private String value(CSVRecord record, String... columnAliases) {
        for (String alias : columnAliases) {
            String actualHeader = record.getParser().getHeaderNames().stream()
                    .filter(header -> header.trim().equalsIgnoreCase(alias.trim()))
                    .findFirst()
                    .orElse(null);
            if (actualHeader != null && record.isSet(actualHeader) && !record.get(actualHeader).isBlank()) {
                return record.get(actualHeader).trim();
            }
        }
        return null;
    }

    private Boolean parseStatusToBoolean(String value) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "true", "1", "a", "activo", "activos" -> true;
            case "false", "0", "i", "inactivo", "inactivos" -> false;
            default -> throw badRequest("Valor inválido para status/estado: " + value);
        };
    }

    private String normalizeEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return "A";
        }
        return switch (estado.trim().toLowerCase(Locale.ROOT)) {
            case "true", "1", "a", "activo", "activos" -> "A";
            case "false", "0", "i", "inactivo", "inactivos" -> "I";
            default -> estado.trim().toUpperCase(Locale.ROOT);
        };
    }

    // ─────────────────────────────── EXPORTACIÓN EXCEL ───────────────────────

    @Override
    public Mono<byte[]> exportExcel() {
        return exportExcel((String) null);
    }

    @Override
    public Mono<byte[]> exportExcel(String status) {
        log.info("Exportando clientes a Excel con estado: {}", status);
        return customersForExport(status)
                .flatMap(customers -> Mono.fromCallable(() -> createExcel(customers))
                        .subscribeOn(Schedulers.boundedElastic()));
    }

    private byte[] createExcel(List<Customer> customers) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Clientes");
            String[] headers = {"id", "first_name", "last_name", "document_type", "document_number",
                    "phone", "email", "registration_date", "status", "created_at", "update_at",
                    "deleted_at", "restored_at"};
            Row header = sheet.createRow(0);
            for (int column = 0; column < headers.length; column++) {
                header.createCell(column).setCellValue(headers[column]);
            }
            for (int index = 0; index < customers.size(); index++) {
                Customer customer = customers.get(index);
                Row row = sheet.createRow(index + 1);
                Object[] values = {
                        customer.getId(),
                        customer.getFirstName(),
                        customer.getLastName(),
                        customer.getDocumentType(),
                        customer.getDocumentNumber(),
                        customer.getPhoneFormatted(),
                        customer.getEmail(),
                        customer.getRegistrationDate() != null ? customer.getRegistrationDate().toString() : null,
                        customer.getEstado(),
                        customer.getCreatedAt() != null ? customer.getCreatedAt().toString() : null,
                        customer.getUpdateAt() != null ? customer.getUpdateAt().toString() : null,
                        customer.getDeletedAt() != null ? customer.getDeletedAt().toString() : null,
                        customer.getRestoredAt() != null ? customer.getRestoredAt().toString() : null
                };
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

    // ─────────────────────────────── EXPORTACIÓN PDF ─────────────────────────

    @Override
    public Mono<byte[]> exportPdf() {
        return exportPdf((String) null, "TODOS");
    }

    @Override
    public Mono<byte[]> exportPdf(String status, String estadoReporte) {
        log.info("Exportando clientes a PDF con estado: {}", status);
        return customersForExport(status)
                .flatMap(customers -> Mono.fromCallable(() -> createPdf(customers, estadoReporte))
                        .subscribeOn(Schedulers.boundedElastic()));
    }

    private byte[] createPdf(List<Customer> customers, String estadoReporte) throws IOException {
        Context context = new Context(Locale.forLanguageTag("es-PE"));
        context.setVariable("customers", customers);
        context.setVariable("clients", customers);
        context.setVariable("estadoReporte", estadoReporte);
        context.setVariable("fechaGeneracion", LocalDateTime.now().format(REPORT_DATE_FORMAT));
        String html = TEMPLATE_ENGINE.process("customer", context);

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

    private Mono<List<Customer>> customersForExport(String status) {
        String normalizedStatus = status != null ? normalizeEstado(status) : null;
        return customerRepository.findAll()
                .filter(customer -> normalizedStatus == null || normalizedStatus.equalsIgnoreCase(customer.getEstado()))
                .collectList()
                .flatMap(customers -> customers.size() < 10
                        ? Mono.error(badRequest("La exportación requiere al menos 10 registros para el estado seleccionado; encontrados: " + customers.size()))
                        : Mono.just(customers));
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

}
