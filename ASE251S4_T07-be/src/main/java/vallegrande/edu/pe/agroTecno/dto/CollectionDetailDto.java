package vallegrande.edu.pe.agroTecno.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "DTO para el detalle de cobranza")
public class CollectionDetailDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Integer detailId;

    @Schema(description = "ID de la cabecera de pago asociada", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer collectionId;

    @Schema(description = "Número de línea correlativo", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer lineNumber;

    @Schema(description = "Descripción del detalle", example = "Cobro de abono inicial", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @Schema(description = "Estado activo/inactivo del detalle", example = "true", accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean status;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, nullable = true)
    private LocalDateTime updatedAt;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, nullable = true)
    private LocalDateTime deletedAt;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, nullable = true)
    private LocalDateTime restoredAt;

    public CollectionDetailDto() {}

    public CollectionDetailDto(Integer detailId, Integer collectionId, Integer lineNumber, String description, Boolean status, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt, LocalDateTime restoredAt) {
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
