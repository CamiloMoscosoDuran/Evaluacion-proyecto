package vallegrande.edu.pe.agroTecno.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "field_meeting")
@Schema(description = "Reunión o visita de campo almacenada en MongoDB")
public class FieldMeeting {

    @Id
    @Field("_id")
    @JsonProperty("_id")
    @JsonAlias({"_id", "id", "meetingId"})
    @Schema(description = "Identificador personalizado", example = "MET001", pattern = "^MET[0-9]{3,}$", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Field("meeting_date_time")
    @JsonProperty("meeting_date_time")
    @Schema(example = "2026-09-25T10:30:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime meetingDateTime;

    @Field("customer_id")
    @JsonProperty("customer_id")
    @Schema(example = "CUS003", requiredMode = Schema.RequiredMode.REQUIRED)
    private String customerId;

    @Field("farm_name")
    @JsonProperty("farm_name")
    @Schema(example = "Fundo El Valle", requiredMode = Schema.RequiredMode.REQUIRED)
    private String farmName;

    @Field("farm_address")
    @JsonProperty("farm_address")
    @Schema(description = "Dirección de la finca como objeto JSON", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, Object> farmAddress;

    @Field("order_sales_id")
    @JsonProperty("order_sales_id")
    @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderSalesId;

    @Field("fruit_quality")
    @JsonProperty("fruit_quality")
    private String fruitQuality;

    @Field("observation")
    @JsonProperty("observation")
    private String observation;

    @Field("visit_status")
    @JsonProperty("visit_status")
    @Schema(description = "Estado de la visita", allowableValues = {"Pendiente", "Completa", "Pospuesta", "Cancelada"}, example = "Pendiente", requiredMode = Schema.RequiredMode.REQUIRED)
    private String visitStatus;

    @Field("created_at")
    @JsonProperty("created_at")
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Field("update_at")
    @JsonProperty("update_at")
    @JsonAlias({"update_at", "updated_at", "updateAt", "updatedAt"})
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updateAt;

    @Field("deleted_at")
    @JsonProperty("deleted_at")
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, nullable = true)
    private LocalDateTime deletedAt;

    @Field("restored_at")
    @JsonProperty("restored_at")
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, nullable = true)
    private LocalDateTime restoredAt;

    public FieldMeeting() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public LocalDateTime getMeetingDateTime() { return meetingDateTime; }
    public void setMeetingDateTime(LocalDateTime meetingDateTime) { this.meetingDateTime = meetingDateTime; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getFarmName() { return farmName; }
    public void setFarmName(String farmName) { this.farmName = farmName; }
    public Map<String, Object> getFarmAddress() { return farmAddress; }
    public void setFarmAddress(Map<String, Object> farmAddress) { this.farmAddress = farmAddress; }
    public String getOrderSalesId() { return orderSalesId; }
    public void setOrderSalesId(String orderSalesId) { this.orderSalesId = orderSalesId; }
    public String getFruitQuality() { return fruitQuality; }
    public void setFruitQuality(String fruitQuality) { this.fruitQuality = fruitQuality; }
    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }
    public String getVisitStatus() { return visitStatus; }
    public void setVisitStatus(String visitStatus) { this.visitStatus = visitStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdateAt() { return updateAt; }
    public void setUpdateAt(LocalDateTime updateAt) { this.updateAt = updateAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
    public LocalDateTime getRestoredAt() { return restoredAt; }
    public void setRestoredAt(LocalDateTime restoredAt) { this.restoredAt = restoredAt; }
}
