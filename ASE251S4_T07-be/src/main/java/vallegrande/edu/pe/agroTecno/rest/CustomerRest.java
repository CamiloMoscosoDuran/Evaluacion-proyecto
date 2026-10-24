package vallegrande.edu.pe.agroTecno.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.agroTecno.model.Customer;
import vallegrande.edu.pe.agroTecno.service.CustomerService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/v1/api/customer")
public class CustomerRest {

    private final CustomerService customerService;

    @Autowired
    public CustomerRest(CustomerService customerService) {
        this.customerService = customerService;
    }

    // GET - Listar todos los clientes (FLUX)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Customer> findAll() {
        return customerService.findAll();
    }

    // GET - Listar por estado: A (activo) o I (inactivo), o boolean true/false (FLUX)
    @GetMapping(value = "/estado/{estado}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Customer> findByEstado(@PathVariable String estado) {
        return customerService.findByEstado(estado);
    }

    // GET - Listar por ID (MONO)
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Customer> findById(@PathVariable String id) {
        return customerService.findById(id);
    }

    // POST - Crear cliente (MONO)
    @PostMapping
    public Mono<Customer> save(@RequestBody Customer customer) {
        return customerService.save(customer);
    }

    // PUT - Editar cliente (MONO)
    @PutMapping("/{id}")
    public Mono<Customer> update(@PathVariable String id, @RequestBody Customer customer) {
        return customerService.update(id, customer);
    }

    // PATCH - Eliminar logico (MONO)
    @PatchMapping("/delete/{id}")
    public Mono<Customer> delete(@PathVariable String id) {
        return customerService.delete(id);
    }

    // PATCH - Restaurar logico (MONO)
    @PatchMapping("/restore/{id}")
    public Mono<Customer> restore(@PathVariable String id) {
        return customerService.restore(id);
    }

    // ─────────────────────────────── IMPORTACIÓN CSV ─────────────────────────

    @PostMapping(value = "/importar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<List<Customer>> importCsv(@RequestPart("file") FilePart file) {
        if (!file.filename().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Solo se admiten archivos CSV"));
        }
        return DataBufferUtils.join(file.content())
                .map(buffer -> {
                    try {
                        byte[] content = new byte[buffer.readableByteCount()];
                        buffer.read(content);
                        return content;
                    } finally {
                        DataBufferUtils.release(buffer);
                    }
                })
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "El archivo CSV está vacío")))
                .flatMap(customerService::importCsv);
    }

    // ─────────────────────────────── EXPORTACIÓN EXCEL ───────────────────────

    @GetMapping("/exportar/excel")
    public Mono<ResponseEntity<byte[]>> exportExcel() {
        return exportExcel(null, "TODOS");
    }

    @GetMapping("/exportar/excel/{estado}")
    public Mono<ResponseEntity<byte[]>> exportExcelByEstado(@PathVariable String estado) {
        ExportFilter filter = parseExportFilter(estado);
        return exportExcel(filter.status(), filter.label());
    }

    private Mono<ResponseEntity<byte[]>> exportExcel(String status, String label) {
        String filename = "clientes_" + label.toLowerCase(Locale.ROOT) + ".xlsx";
        return customerService.exportExcel(status).map(content -> ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(filename, StandardCharsets.UTF_8).build().toString())
                .body(content));
    }

    // ─────────────────────────────── EXPORTACIÓN PDF ─────────────────────────

    @GetMapping("/exportar/pdf")
    public Mono<ResponseEntity<byte[]>> exportPdf() {
        return exportPdf(null, "TODOS");
    }

    @GetMapping("/exportar/pdf/{estado}")
    public Mono<ResponseEntity<byte[]>> exportPdfByEstado(@PathVariable String estado) {
        ExportFilter filter = parseExportFilter(estado);
        return exportPdf(filter.status(), filter.label());
    }

    private Mono<ResponseEntity<byte[]>> exportPdf(String status, String label) {
        String filename = "clientes_" + label.toLowerCase(Locale.ROOT) + ".pdf";
        return customerService.exportPdf(status, label).map(content -> ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(filename, StandardCharsets.UTF_8).build().toString())
                .body(content));
    }

    // ─────────────────────────────── HELPERS ─────────────────────────────────

    private ExportFilter parseExportFilter(String estado) {
        return switch (estado.trim().toLowerCase(Locale.ROOT)) {
            case "true", "1", "a", "activo", "activos" -> new ExportFilter("A", "ACTIVOS");
            case "false", "0", "i", "inactivo", "inactivos" -> new ExportFilter("I", "INACTIVOS");
            case "todos", "todo", "all" -> new ExportFilter(null, "TODOS");
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Estado inválido. Use activos, inactivos o todos");
        };
    }

    private record ExportFilter(String status, String label) {}

}
