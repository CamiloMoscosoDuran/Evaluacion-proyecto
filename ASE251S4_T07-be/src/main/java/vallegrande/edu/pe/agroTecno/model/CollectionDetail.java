package vallegrande.edu.pe.agroTecno.model;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table(value = "collection_detail", schema = "cobranzas")
@Schema(description = "Detalle de cobranza asociado a una orden de venta")
public class CollectionDetail {

    @Id
    @Column("detail_id")
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Integer detailId;

    @Column("collection_id")
    @Schema(description = "ID de la cabecera de pago asociada", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer collectionId;

    @Column("line_number")
    @Schema(description = "Número de línea correlativo", example = "1")
    private Integer lineNumber;

    @Column("description")
    @Schema(description = "Descripción del detalle", example = "Cobro de abono inicial", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @Column("status")
    @Schema(description = "Estado activo/inactivo del detalle", example = "true")
    private Boolean status;

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

    public CollectionDetail() {}

    public CollectionDetail(Integer detailId, Integer collectionId, Integer lineNumber, String description, Boolean status, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt, LocalDateTime restoredAt) {
        this.detailId = detailId;
        this.collectionId = collectionId;
        this.lineNumber = lineNumber;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.restoredAt = restoredAt;
    }

    public Integer getDetailId() { return detailId; }
    public void setDetailId(Integer detailId) { this.detailId = detailId; }

    public Integer getCollectionId() { return collectionId; }
    public void setCollectionId(Integer collectionId) { this.collectionId = collectionId; }

    public Integer getLineNumber() { return lineNumber; }
    public void setLineNumber(Integer lineNumber) { this.lineNumber = lineNumber; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public LocalDateTime getRestoredAt() { return restoredAt; }
    public void setRestoredAt(LocalDateTime restoredAt) { this.restoredAt = restoredAt; }
}
