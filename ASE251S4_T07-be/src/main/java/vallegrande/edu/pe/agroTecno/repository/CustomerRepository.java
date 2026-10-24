package vallegrande.edu.pe.agroTecno.repository;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.agroTecno.model.Customer;

public interface CustomerRepository extends ReactiveMongoRepository<Customer, String> {

    // Filtrar por status boolean (true = activo, false = inactivo)
    Flux<Customer> findByStatus(Boolean status);

    // Búsqueda por documento único (índice uq_customer_document_number)
    Mono<Customer> findByDocumentNumber(String documentNumber);

    // Búsqueda por email único (índice uq_customer_email)
    Mono<Customer> findByEmail(String email);

    // Búsqueda por tipo de cultivo en parcelas (índice multikey ix_customer_type_crop)
    @Query("{ 'farm_plot.type_crop': { $regex: ?0, $options: 'i' } }")
    Flux<Customer> findByCropType(String typeCrop);

}
