package vallegrande.edu.pe.agroTecno.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.agroTecno.model.SalesOrder;

public interface SalesOrderRepository extends ReactiveCrudRepository<SalesOrder, Integer> {

    @Query("SELECT * FROM ventas.sales_order WHERE order_id = :id")
    Mono<SalesOrder> findById(Integer id);
}
