package vallegrande.edu.pe.agroTecno.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.agroTecno.model.OrderDetail;

public interface OrderDetailRepository extends ReactiveCrudRepository<OrderDetail, Integer> {

    @Query("SELECT * FROM ventas.order_detail WHERE order_detail_id = :id")
    Mono<OrderDetail> findById(Integer id);

    @Query("SELECT * FROM ventas.order_detail WHERE sales_order_order_id = :orderId")
    Flux<OrderDetail> findBySalesOrderOrderId(Integer orderId);

    @Query("DELETE FROM ventas.order_detail WHERE sales_order_order_id = :orderId")
    Mono<Void> deleteBySalesOrderOrderId(Integer orderId);
}
