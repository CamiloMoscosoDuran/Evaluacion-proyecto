package vallegrande.edu.pe.agroTecno.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "formula")
@Schema(description = "Fórmula de producción almacenada en MongoDB")
public class Formula {

    @Id
    @Field("_id")
    @JsonProperty("_id")
    @Schema(description = "ID autogenerado con formato FOR###", example = "FOR001", pattern = "^FOR[0-9]{3,}$", accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @Field("name")
    @JsonProperty("name")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Field("description")
    @JsonProperty("description")
    private String description;

    @Field("standard_batch")
    @JsonProperty("standard_batch")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer standardBatch;

    @Field("unit")
    @JsonProperty("unit")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String unit;

    @Field("production_time")
    @JsonProperty("production_time")
    private Integer productionTime;

    @Field(value = "preparation_cost", targetType = FieldType.DECIMAL128)
    @JsonProperty("preparation_cost")
    private BigDecimal preparationCost;

    @Field(value = "suggested_price", targetType = FieldType.DECIMAL128)
    @JsonProperty("suggested_price")
    private BigDecimal suggestedPrice;

    @Field("status")
    @JsonProperty("status")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean status;

    @Field("recipe")
    @JsonProperty("recipe")
    private Recipe recipe;

    @Field("formula_inventory")
    @JsonProperty("formula_inventory")
    private List<FormulaInventory> formulaInventory;

    @Field("created_at")
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @Field("update_at")
    @JsonProperty("update_at")
    private LocalDateTime updateAt;

    @Field("deleted_at")
    @JsonProperty("deleted_at")
    private LocalDateTime deletedAt;

    @Field("restored_at")
    @JsonProperty("restored_at")
    private LocalDateTime restoredAt;

    public Formula() {}

    public Formula(String id, String name, String description, Integer standardBatch, String unit, Integer productionTime,
                   BigDecimal preparationCost, BigDecimal suggestedPrice, Boolean status, Recipe recipe,
                   List<FormulaInventory> formulaInventory, LocalDateTime createdAt, LocalDateTime updateAt,
                   LocalDateTime deletedAt, LocalDateTime restoredAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.standardBatch = standardBatch;
        this.unit = unit;
        this.productionTime = productionTime;
        this.preparationCost = preparationCost;
        this.suggestedPrice = suggestedPrice;
        this.status = status;
        this.recipe = recipe;
        this.formulaInventory = formulaInventory;
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

    public Integer getStandardBatch() { return standardBatch; }
    public void setStandardBatch(Integer standardBatch) { this.standardBatch = standardBatch; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public Integer getProductionTime() { return productionTime; }
    public void setProductionTime(Integer productionTime) { this.productionTime = productionTime; }

    public BigDecimal getPreparationCost() { return preparationCost; }
    public void setPreparationCost(BigDecimal preparationCost) { this.preparationCost = preparationCost; }

    public BigDecimal getSuggestedPrice() { return suggestedPrice; }
    public void setSuggestedPrice(BigDecimal suggestedPrice) { this.suggestedPrice = suggestedPrice; }

    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }

    public Recipe getRecipe() { return recipe; }
    public void setRecipe(Recipe recipe) { this.recipe = recipe; }

    public List<FormulaInventory> getFormulaInventory() { return formulaInventory; }
    public void setFormulaInventory(List<FormulaInventory> formulaInventory) { this.formulaInventory = formulaInventory; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdateAt() { return updateAt; }
    public void setUpdateAt(LocalDateTime updateAt) { this.updateAt = updateAt; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public LocalDateTime getRestoredAt() { return restoredAt; }
    public void setRestoredAt(LocalDateTime restoredAt) { this.restoredAt = restoredAt; }

    public static class Recipe {
        @Field("version")
        @JsonProperty("version")
        private String version;

        @Field("ingredients")
        @JsonProperty("ingredients")
        private List<Ingredient> ingredients;

        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }

        public List<Ingredient> getIngredients() { return ingredients; }
        public void setIngredients(List<Ingredient> ingredients) { this.ingredients = ingredients; }
    }

    public static class Ingredient {
        @Field("supply_id")
        @JsonProperty("supply_id")
        private String supplyId;

        @Field("supply_name")
        @JsonProperty("supply_name")
        private String supplyName;

        @Field(value = "quantity_required", targetType = FieldType.DECIMAL128)
        @JsonProperty("quantity_required")
        private BigDecimal quantityRequired;

        @Field(value = "percentage", targetType = FieldType.DECIMAL128)
        @JsonProperty("percentage")
        private BigDecimal percentage;

        public String getSupplyId() { return supplyId; }
        public void setSupplyId(String supplyId) { this.supplyId = supplyId; }

        public String getSupplyName() { return supplyName; }
        public void setSupplyName(String supplyName) { this.supplyName = supplyName; }

        public BigDecimal getQuantityRequired() { return quantityRequired; }
        public void setQuantityRequired(BigDecimal quantityRequired) { this.quantityRequired = quantityRequired; }

        public BigDecimal getPercentage() { return percentage; }
        public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }
    }

    public static class FormulaInventory {
        @Field("gallon_type")
        @JsonProperty("gallon_type")
        private Integer gallonType;

        @Field("stock_quantity")
        @JsonProperty("stock_quantity")
        private Integer stockQuantity;

        @Field("last_update")
        @JsonProperty("last_update")
        private LocalDate lastUpdate;

        public Integer getGallonType() { return gallonType; }
        public void setGallonType(Integer gallonType) { this.gallonType = gallonType; }

        public Integer getStockQuantity() { return stockQuantity; }
        public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

        public LocalDate getLastUpdate() { return lastUpdate; }
        public void setLastUpdate(LocalDate lastUpdate) { this.lastUpdate = lastUpdate; }
    }
}