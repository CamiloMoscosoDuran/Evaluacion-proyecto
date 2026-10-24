package vallegrande.edu.pe.agroTecno.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import io.swagger.v3.oas.annotations.media.Schema;

@Table(value = "installment", schema = "cobranzas")
@Schema(description = "Cuota de una cobranza")
public class Installment {

    @Id
    @Column("installment_id")
    @Schema(description = "Identificador generado por SQL Server", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer installmentId;

    @Column("collection_id")
    @Schema(description = "ID asignado desde la ruta de la cobranza", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer collectionId;

    @Column("installment_num")
    @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer installmentNum;

    @Column("due_date")
    @Schema(example = "2026-10-15", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate dueDate;

    @Column("amount_due")
    @Schema(example = "300.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amountDue;

    @Column("amount_paid")
    @Schema(example = "0.00")
    private BigDecimal amountPaid;

    @Column("payment_type")
    @Schema(example = "EFECTIVO")
    private String paymentType;

    @Column("payment_date")
    @Schema(example = "2026-10-10")
    private LocalDate paymentDate;

    @Column("observation")
    @Schema(example = "Cuota por pagar")
    private String observation;

    @Column("status")
    @Schema(description = "P: pendiente, C: baja lógica, A: pagado", allowableValues = {"P", "C", "A"}, example = "P", accessMode = Schema.AccessMode.READ_ONLY)
    private String status;

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

    public Installment() {}

    public Integer getInstallmentId() { return installmentId; }
    public void setInstallmentId(Integer installmentId) { this.installmentId = installmentId; }

    public Integer getCollectionId() { return collectionId; }
    public void setCollectionId(Integer collectionId) { this.collectionId = collectionId; }

    public Integer getInstallmentNum() { return installmentNum; }
    public void setInstallmentNum(Integer installmentNum) { this.installmentNum = installmentNum; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public BigDecimal getAmountDue() { return amountDue; }
    public void setAmountDue(BigDecimal amountDue) { this.amountDue = amountDue; }

    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }

    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public LocalDateTime getRestoredAt() { return restoredAt; }
    public void setRestoredAt(LocalDateTime restoredAt) { this.restoredAt = restoredAt; }
}
