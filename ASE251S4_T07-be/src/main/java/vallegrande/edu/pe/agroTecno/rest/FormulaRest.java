package vallegrande.edu.pe.agroTecno.rest;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.agroTecno.model.Formula;
import vallegrande.edu.pe.agroTecno.service.FormulaService;

import java.nio.charset.StandardCharsets;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/v1/api/formula")
public class FormulaRest {

    private final FormulaService formulaService;

    @Autowired
    public FormulaRest(FormulaService formulaService) {
        this.formulaService = formulaService;
    }

    // GET - Listar todos los formulas (FLUX)
    @GetMapping
    public Flux<Formula> findAll() {
        return formulaService.findAll();
    }

    // GET - Listar por estado: A (activo) o I (inactivo) (FLUX)
    @GetMapping("/estado/{estado}")
    public Flux<Formula> findByEstado(@PathVariable String estado) {
        return formulaService.findByEstado(estado);
    }

    // GET - Listar por ID (MONO)
    @GetMapping("/{id}")
    public Mono<Formula> findById(@PathVariable String id) {
        return formulaService.findById(id);
    }

    // POST - Crear formula (MONO)
    @PostMapping
    public Mono<Formula> save(@RequestBody Formula formula) {
        return formulaService.save(formula);
    }

    // PUT - Editar formula (MONO)
    @PutMapping("/{id}")
    public Mono<Formula> update(@PathVariable String id, @RequestBody Formula formula) {
        return formulaService.update(id, formula);
    }

    // PATCH - Eliminar logico (MONO)
    @PatchMapping("/delete/{id}")
    public Mono<Formula> delete(@PathVariable String id) {
        return formulaService.delete(id);
    }

    // PATCH - Restaurar logico (MONO)
    @PatchMapping("/restore/{id}")
    public Mono<Formula> restore(@PathVariable String id) {
        return formulaService.restore(id);
    }

    @PostMapping(value = "/importar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<List<Formula>> importCsv(@RequestPart("file") FilePart file) {
        if (!file.filename().toLowerCase(java.util.Locale.ROOT).endsWith(".csv")) {
            return Mono.error(new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Solo se admiten archivos CSV"));
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
                .switchIfEmpty(Mono.error(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST, "El archivo CSV está vacío")))
                .flatMap(formulaService::importCsv);
    }

    @GetMapping("/exportar/excel")
    public Mono<ResponseEntity<byte[]>> exportExcel() {
        return exportExcel(null, "TODOS");
    }

    @GetMapping("/exportar/excel/{estado}")
    public Mono<ResponseEntity<byte[]>> exportExcelByEstado(@PathVariable String estado) {
        ExportFilter filter = parseExportFilter(estado);
        return exportExcel(filter.status(), filter.label());
    }

    private Mono<ResponseEntity<byte[]>> exportExcel(Boolean status, String label) {
        String filename = "formulas_" + label.toLowerCase(java.util.Locale.ROOT) + ".xlsx";
        return formulaService.exportExcel(status).map(content -> ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(filename, StandardCharsets.UTF_8).build().toString())
                .body(content));
    }

    @GetMapping("/exportar/pdf")
    public Mono<ResponseEntity<byte[]>> exportPdf() {
        return exportPdf(null, "TODOS");
    }

    @GetMapping("/exportar/pdf/{estado}")
    public Mono<ResponseEntity<byte[]>> exportPdfByEstado(@PathVariable String estado) {
        ExportFilter filter = parseExportFilter(estado);
        return exportPdf(filter.status(), filter.label());
    }

    private Mono<ResponseEntity<byte[]>> exportPdf(Boolean status, String label) {
        String filename = "formulas_" + label.toLowerCase(java.util.Locale.ROOT) + ".pdf";
        return formulaService.exportPdf(status, label).map(content -> ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(filename, StandardCharsets.UTF_8).build().toString())
                .body(content));
    }

    private ExportFilter parseExportFilter(String estado) {
        return switch (estado.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "true", "1", "a", "activo", "activos" -> new ExportFilter(Boolean.TRUE, "ACTIVOS");
            case "false", "0", "i", "inactivo", "inactivos" -> new ExportFilter(Boolean.FALSE, "INACTIVOS");
            case "todos", "todo", "all" -> new ExportFilter(null, "TODOS");
            default -> throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Estado inválido. Use activos, inactivos o todos");
        };
    }

    private record ExportFilter(Boolean status, String label) {}

}