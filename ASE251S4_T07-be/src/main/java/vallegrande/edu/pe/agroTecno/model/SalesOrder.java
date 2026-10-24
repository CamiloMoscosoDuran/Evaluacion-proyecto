package vallegrande.edu.pe.agroTecno.model;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(value = "sales_order", schema = "ventas")
@Schema(description = "Orden de venta almacenada en SQL Server")
public class SalesOrder {

    @Id
    @Column("order_id")
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Integer orderId;

    @ReadOnlyProperty
    @Column("referral_guide")
    @Schema(description = "Guía de remisión", example = "GR-001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String referralGuide;

    @Column("mongo_customer_id")
    @Schema(description = "ID del cliente en MongoDB", example = "CUS003", requiredMode = Schema.RequiredMode.REQUIRED)
    private String mongoCustomerId;

    @Column("entry_date")
    @Schema(description = "Fecha de entrada/registro de la orden", example = "2026-10-01T10:00:00")
    private LocalDateTime entryDate;

    @Column("total_order")
    @Schema(description = "Monto total de la orden", example = "1500.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal totalOrder;

    @Column("order_status")
    @Schema(description = "P: pendiente, C: completado, A: anulado", allowableValues = {"P", "C", "A"}, example = "A", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderStatus;

    @Column("created_at")
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Column("updated_at")
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, nullable = true)
    private LocalDateTime updatedAt;

    @Column("deleted_at")
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, nullable = true)
    private LocalDateTime deletedAt;

    @Column("restored_at")
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, nullable = true)
    private LocalDateTime restoredAt;

    public SalesOrder() {}

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }

    public String getReferralGuide() { return referralGuide; }
    public void setReferralGuide(String referralGuide) { this.referralGuide = referralGuide; }

    public String getMongoCustomerId() { return mongoCustomerId; }
    public void setMongoCustomerId(String mongoCustomerId) { this.mongoCustomerId = mongoCustomerId; }

    public LocalDateTime getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDateTime entryDate) { this.entryDate = entryDate; }

    public BigDecimal getTotalOrder() { return totalOrder; }
    public void setTotalOrder(BigDecimal totalOrder) { this.totalOrder = totalOrder; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public LocalDateTime getRestoredAt() { return restoredAt; }
    public void setRestoredAt(LocalDateTime restoredAt) { this.restoredAt = restoredAt; }
}
