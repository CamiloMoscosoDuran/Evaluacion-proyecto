package vallegrande.edu.pe.agroTecno.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.agroTecno.model.Supply;
import vallegrande.edu.pe.agroTecno.service.SupplyService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/v1/api/supply")
public class SupplyRest {

    private final SupplyService supplyService;

    @Autowired
    public SupplyRest(SupplyService supplyService) {
        this.supplyService = supplyService;
    }

    // ─────────────────────────────── CRUD ────────────────────────────────────

    // GET - Listar todos los insumos (FLUX)
    @GetMapping
    public Flux<Supply> findAll() {
        return supplyService.findAll();
    }

    // GET - Listar por estado: true (activo) / false (inactivo) (FLUX)
    @GetMapping("/estado/{estado}")
    public Flux<Supply> findByEstado(@PathVariable Boolean estado) {
        return supplyService.findByStatus(estado);
    }

    // GET - Listar por categoría (FLUX)
    @GetMapping("/categoria/{categoria}")
    public Flux<Supply> findByCategoria(@PathVariable String categoria) {
        return supplyService.findByCategory(categoria);
    }

    // GET - Listar por ID (MONO)
    @GetMapping("/{id}")
    public Mono<Supply> findById(@PathVariable String id) {
        return supplyService.findById(id);
    }

    // POST - Crear insumo (MONO)
    @PostMapping
    public Mono<Supply> save(@RequestBody Supply supply) {
        return supplyService.save(supply);
    }

    // PUT - Editar insumo (MONO)
    @PutMapping("/{id}")
    public Mono<Supply> update(@PathVariable String id, @RequestBody Supply supply) {
        return supplyService.update(id, supply);
    }

    // PATCH - Eliminar logico (MONO)
    @PatchMapping("/delete/{id}")
    public Mono<Supply> delete(@PathVariable String id) {
        return supplyService.delete(id);
    }

    // PATCH - Restaurar logico (MONO)
    @PatchMapping("/restore/{id}")
    public Mono<Supply> restore(@PathVariable String id) {
        return supplyService.restore(id);
    }

    // ─────────────────────────────── IMPORTACIÓN ─────────────────────────────

    @PostMapping(value = "/importar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<List<Supply>> importar(@RequestPart("file") FilePart file) {
        String filename = file.filename().toLowerCase(Locale.ROOT);

        boolean isCsv = filename.endsWith(".csv");
        boolean isExcel = filename.endsWith(".xlsx") || filename.endsWith(".xls");

        if (!isCsv && !isExcel) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Solo se admiten archivos CSV (.csv) o Excel (.xlsx / .xls)"));
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
                        HttpStatus.BAD_REQUEST, "El archivo está vacío")))
                .flatMap(content -> isCsv
                        ? supplyService.importCsv(content)
                        : supplyService.importExcel(content));
    }

    // ─────────────────────────────── EXPORTACIÓN EXCEL ───────────────────────

    @GetMapping("/exportar/excel")
    public Mono<ResponseEntity<byte[]>> exportExcel() {
        return buildExcelResponse(null, "TODOS");
    }

    @GetMapping("/exportar/excel/{estado}")
    public Mono<ResponseEntity<byte[]>> exportExcelByEstado(@PathVariable String estado) {
        ExportFilter filter = parseFilter(estado);
        return buildExcelResponse(filter.status(), filter.label());
    }

    private Mono<ResponseEntity<byte[]>> buildExcelResponse(Boolean status, String label) {
        String filename = "insumos_" + label.toLowerCase(Locale.ROOT) + ".xlsx";
        return supplyService.exportExcel(status).map(content ->
                ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                ContentDisposition.attachment()
                                        .filename(filename, StandardCharsets.UTF_8)
                                        .build().toString())
                        .body(content));
    }

    // ─────────────────────────────── EXPORTACIÓN PDF ─────────────────────────

    @GetMapping("/exportar/pdf")
    public Mono<ResponseEntity<byte[]>> exportPdf() {
        return buildPdfResponse(null, "TODOS");
    }

    @GetMapping("/exportar/pdf/{estado}")
    public Mono<ResponseEntity<byte[]>> exportPdfByEstado(@PathVariable String estado) {
        ExportFilter filter = parseFilter(estado);
        return buildPdfResponse(filter.status(), filter.label());
    }

    private Mono<ResponseEntity<byte[]>> buildPdfResponse(Boolean status, String label) {
        String filename = "insumos_" + label.toLowerCase(Locale.ROOT) + ".pdf";
        return supplyService.exportPdf(status, label).map(content ->
                ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                ContentDisposition.attachment()
                                        .filename(filename, StandardCharsets.UTF_8)
                                        .build().toString())
                        .body(content));
    }

    // ─────────────────────────────── HELPERS ─────────────────────────────────

    private ExportFilter parseFilter(String estado) {
        return switch (estado.trim().toLowerCase(Locale.ROOT)) {
            case "activos", "activo", "true", "1" -> new ExportFilter(Boolean.TRUE, "ACTIVOS");
            case "inactivos", "inactivo", "false", "0" -> new ExportFilter(Boolean.FALSE, "INACTIVOS");
            case "todos", "todo", "all" -> new ExportFilter(null, "TODOS");
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Estado inválido. Valores permitidos: activos, inactivos, todos");
        };
    }

    private record ExportFilter(Boolean status, String label) {}
}
