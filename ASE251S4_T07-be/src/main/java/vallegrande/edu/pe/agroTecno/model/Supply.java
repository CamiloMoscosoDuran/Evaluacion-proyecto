package vallegrande.edu.pe.agroTecno.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "supply")
@Schema(description = "Insumo / Suministro almacenado en MongoDB")
public class Supply {

    @Id
    @Field("_id")
    @JsonProperty("id")
    @JsonAlias({"_id", "id", "supplyId"})
    @Schema(description = "Identificador personalizado", example = "SUP001", pattern = "^SUP[0-9]{3,}$", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Field("name")
    @JsonProperty("name")
    @Schema(description = "Nombre del insumo", example = "Nitrato de Amonio", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Field("description")
    @JsonProperty("description")
    @Schema(description = "Descripción del insumo", example = "Aporte rápido de nitrógeno para crecimiento foliar")
    private String description;

    @Field("category")
    @JsonProperty("category")
    @Schema(description = "Categoría del insumo", example = "Fertilizantes", requiredMode = Schema.RequiredMode.REQUIRED)
    private String category = "General";

    @Field("inventory")
    @JsonProperty("inventory")
    @Schema(description = "Información de inventario y vencimiento", requiredMode = Schema.RequiredMode.REQUIRED)
    private Inventory inventory;

    @Field("unit")
    @JsonProperty("unit")
    @Schema(description = "Unidad de medida (kg, litro, etc.)", example = "kg", requiredMode = Schema.RequiredMode.REQUIRED)
    private String unit;

    @Field("location")
    @JsonProperty("location")
    @Schema(description = "Ubicación en almacén", example = "Almacén A - Estante 1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String location;

    @Field("status")
    @JsonProperty("status")
    @JsonAlias({"estado", "status"})
    @Schema(description = "Estado activo/inactivo (true/false)", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean status = true;

    // Auditoría
    @Field("created_at")
    @JsonProperty("created_at")
    @JsonAlias({"createdAt", "created_at"})
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Field("update_at")
    @JsonProperty("update_at")
    @JsonAlias({"update_at", "updated_at", "updateAt", "updatedAt"})
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updateAt;

    @Field("deleted_at")
    @JsonProperty("deleted_at")
    @JsonAlias({"deletedAt", "deleted_at"})
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, nullable = true)
    private LocalDateTime deletedAt;

    @Field("restored_at")
    @JsonProperty("restored_at")
    @JsonAlias({"restoredAt", "restored_at"})
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, nullable = true)
    private LocalDateTime restoredAt;

    // Constructors
    public Supply() {}

    public Supply(String id, String name, String description, String category, Inventory inventory,
                  String unit, String location, Boolean status,
                  LocalDateTime createdAt, LocalDateTime updateAt, LocalDateTime deletedAt, LocalDateTime restoredAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.inventory = inventory;
        this.unit = unit;
        this.location = location;
        this.status = status;
        this.createdAt = createdAt;
        this.updateAt = updateAt;
        this.deletedAt = deletedAt;
        this.restoredAt = restoredAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) {
        this.category = (category == null || category.isBlank()) ? "General" : category;
    }

    public Inventory getInventory() { return inventory; }
    public void setInventory(Inventory inventory) { this.inventory = inventory; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }

    @JsonSetter("status")
    public void setStatusFromJson(Object value) {
        if (value == null) {
            this.status = true;
        } else if (value instanceof Boolean b) {
            this.status = b;
        } else {
            String s = value.toString().trim();
            this.status = "true".equalsIgnoreCase(s) || "1".equals(s) || "A".equalsIgnoreCase(s) || "ACTIVO".equalsIgnoreCase(s);
        }
    }

    @JsonIgnore
    public String getEstado() {
        return Boolean.TRUE.equals(status) ? "A" : "I";
    }

    @JsonIgnore
    public void setEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            this.status = true;
            return;
        }
        String s = estado.trim();
        this.status = "A".equalsIgnoreCase(s) || "ACTIVO".equalsIgnoreCase(s) || "true".equalsIgnoreCase(s) || "1".equals(s);
    }

    @JsonIgnore
    public boolean isActivo() {
        return Boolean.TRUE.equals(status);
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdateAt() { return updateAt; }
    public void setUpdateAt(LocalDateTime updateAt) { this.updateAt = updateAt; }

    @JsonIgnore
    public LocalDateTime getUpdatedAt() { return updateAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updateAt = updatedAt; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public LocalDateTime getRestoredAt() { return restoredAt; }
    public void setRestoredAt(LocalDateTime restoredAt) { this.restoredAt = restoredAt; }
}
