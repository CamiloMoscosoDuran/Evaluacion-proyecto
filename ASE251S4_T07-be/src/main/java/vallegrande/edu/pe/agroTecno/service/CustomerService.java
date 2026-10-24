package vallegrande.edu.pe.agroTecno.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.agroTecno.model.Customer;

import java.util.List;

public interface CustomerService {

    // Listar todos - FLUX
    Flux<Customer> findAll();

    // Listar por estado boolean (true/false) - FLUX
    Flux<Customer> findByStatus(Boolean status);

    // Listar por estado string ("A"/"I", "ACTIVO"/"INACTIVO") - FLUX
    Flux<Customer> findByEstado(String estado);

    // Listar por ID - MONO
    Mono<Customer> findById(String id);

    // Crear - MONO
    Mono<Customer> save(Customer customer);

    // Editar - MONO
    Mono<Customer> update(String id, Customer customer);

    // Eliminar logico - MONO
    Mono<Customer> delete(String id);

    // Restaurar logico - MONO
    Mono<Customer> restore(String id);

    // Importación masiva CSV
    Mono<List<Customer>> importCsv(byte[] content);

    // Exportación a Excel
    Mono<byte[]> exportExcel();

    Mono<byte[]> exportExcel(String status);

    default Mono<byte[]> exportExcel(Boolean status) {
        return exportExcel(status == null ? null : (status ? "A" : "I"));
    }

    // Exportación a PDF mediante plantilla HTML
    Mono<byte[]> exportPdf();

    Mono<byte[]> exportPdf(String status, String estadoReporte);

    default Mono<byte[]> exportPdf(Boolean status, String estadoReporte) {
        return exportPdf(status == null ? null : (status ? "A" : "I"), estadoReporte);
    }

}
