package vallegrande.edu.pe.agroTecno.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import io.swagger.v3.oas.annotations.media.Schema;

@Table(value = "payment_collection", schema = "cobranzas")
@Schema(description = "Cobranza de una orden de venta almacenada en SQL Server")
public class PaymentCollection {

    @Id
    @Column("collection_id")
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Integer collectionId;

    @Column("order_id")
    @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer orderId;

    @Column("mongo_customer_id")
    @Schema(example = "CUS003", requiredMode = Schema.RequiredMode.REQUIRED)
    private String mongoCustomerId;

    @Column("total_amount")
    @Schema(example = "150.50", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal totalAmount;

    @Column("installments_count")
    @Schema(example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer installmentsCount;

    @Column("payment_status")
    @Schema(description = "P: pendiente, C: completado, A: anulado", allowableValues = {"P", "C", "A"}, example = "P", requiredMode = Schema.RequiredMode.REQUIRED)
    private String paymentStatus;

    @Column("created_at")
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Column("updated_at")
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    @Column("deleted_at")
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, nullable = true)
    private LocalDateTime deletedAt;

    @Column("restored_at")
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, nullable = true)
    private LocalDateTime restoredAt;

    public PaymentCollection() {}

    public Integer getCollectionId() { return collectionId; }
    public void setCollectionId(Integer collectionId) { this.collectionId = collectionId; }
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public String getMongoCustomerId() { return mongoCustomerId; }
    public void setMongoCustomerId(String mongoCustomerId) { this.mongoCustomerId = mongoCustomerId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public Integer getInstallmentsCount() { return installmentsCount; }
    public void setInstallmentsCount(Integer installmentsCount) { this.installmentsCount = installmentsCount; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
    public LocalDateTime getRestoredAt() { return restoredAt; }
    public void setRestoredAt(LocalDateTime restoredAt) { this.restoredAt = restoredAt; }
}
