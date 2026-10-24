package vallegrande.edu.pe.agroTecno.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.agroTecno.model.Supply;

import java.util.List;

public interface SupplyService {

    // Listar todos - FLUX
    Flux<Supply> findAll();

    // Listar por estado (true/false) - FLUX
    Flux<Supply> findByStatus(Boolean status);

    // Listar por categoría - FLUX
    Flux<Supply> findByCategory(String category);

    // Listar por ID - MONO
    Mono<Supply> findById(String id);

    // Crear - MONO
    Mono<Supply> save(Supply supply);

    // Editar - MONO
    Mono<Supply> update(String id, Supply supply);

    // Eliminar logico - MONO
    Mono<Supply> delete(String id);

    // Restaurar logico - MONO
    Mono<Supply> restore(String id);

    // Importar desde CSV - MONO
    Mono<List<Supply>> importCsv(byte[] content);

    // Importar desde Excel - MONO
    Mono<List<Supply>> importExcel(byte[] content);

    // Exportar a Excel (todos) - MONO
    Mono<byte[]> exportExcel();

    // Exportar a Excel filtrado por estado - MONO
    Mono<byte[]> exportExcel(Boolean status);

    // Exportar a PDF (todos) - MONO
    Mono<byte[]> exportPdf();

    // Exportar a PDF filtrado por estado - MONO
    Mono<byte[]> exportPdf(Boolean status, String estadoReporte);

}
