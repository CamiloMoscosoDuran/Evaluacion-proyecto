package vallegrande.edu.pe.agroTecno.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Solicitud para crear o editar una orden de venta con sus detalles de productos")
public class SalesOrderRequestDto {

    @Schema(description = "ID del cliente en MongoDB", example = "CUS003", requiredMode = Schema.RequiredMode.REQUIRED)
    private String mongoCustomerId;

    @Schema(description = "Fecha de registro de la orden", example = "2026-10-01T10:00:00")
    private LocalDateTime entryDate;

    @Schema(description = "Calculado por el backend a partir de orderDetails", accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal totalOrder;

    @Schema(description = "P: pendiente, C: completado, A: anulado", allowableValues = {"P", "C", "A"}, example = "A")
    private String orderStatus;

    @JsonProperty("installments_count")
    @Schema(name = "installments_count", description = "Cantidad de cuotas acordadas con el cliente", example = "6", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer installmentsCount;

    @Schema(description = "Detalles de productos de la orden; debe contener al menos uno", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<OrderDetailDto> orderDetails;

    public SalesOrderRequestDto() {}

    public SalesOrderRequestDto(String mongoCustomerId, LocalDateTime entryDate, BigDecimal totalOrder, String orderStatus, List<OrderDetailDto> orderDetails) {
        this.mongoCustomerId = mongoCustomerId;
        this.entryDate = entryDate;
        this.totalOrder = totalOrder;
        this.orderStatus = orderStatus;
        this.orderDetails = orderDetails;
    }

    public String getMongoCustomerId() { return mongoCustomerId; }
    public void setMongoCustomerId(String mongoCustomerId) { this.mongoCustomerId = mongoCustomerId; }

    public LocalDateTime getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDateTime entryDate) { this.entryDate = entryDate; }

    public BigDecimal getTotalOrder() { return totalOrder; }
    public void setTotalOrder(BigDecimal totalOrder) { this.totalOrder = totalOrder; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public Integer getInstallmentsCount() { return installmentsCount; }
    public void setInstallmentsCount(Integer installmentsCount) { this.installmentsCount = installmentsCount; }

    public List<OrderDetailDto> getOrderDetails() { return orderDetails; }
    public void setOrderDetails(List<OrderDetailDto> orderDetails) { this.orderDetails = orderDetails; }
}
