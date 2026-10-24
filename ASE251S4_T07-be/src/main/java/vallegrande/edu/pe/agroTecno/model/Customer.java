package vallegrande.edu.pe.agroTecno.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Document(collection = "customer")
@Schema(description = "Cliente almacenado en MongoDB")
public class Customer {

    @Id
    @Field("_id")
    @JsonProperty("id")
    @JsonAlias({"_id", "id", "customerId", "clientId"})
    @Schema(description = "ID autogenerado con formato CUS###", example = "CUS001", pattern = "^CUS[0-9]{3,}$", accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @Field("first_name")
    @JsonProperty("first_name")
    @JsonAlias({"firstName", "first_name"})
    @Schema(description = "Nombre(s) del cliente", example = "Carlos", requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    @Field("last_name")
    @JsonProperty("last_name")
    @JsonAlias({"lastName", "last_name"})
    @Schema(description = "Apellido(s) del cliente", example = "Mendoza Váldez", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    @Field("document_type")
    @JsonProperty("document_type")
    @JsonAlias({"documentType", "document_type"})
    @Schema(description = "Tipo de documento (DNI, RUC, CE)", example = "DNI", requiredMode = Schema.RequiredMode.REQUIRED)
    private String documentType;

    @Field("document_number")
    @JsonProperty("document_number")
    @JsonAlias({"documentNumber", "document_number"})
    @Schema(description = "Número de documento", example = "45891234", requiredMode = Schema.RequiredMode.REQUIRED)
    private String documentNumber;

    @Field("phone")
    @JsonProperty("phone")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @Schema(description = "Lista de teléfonos del cliente", example = "[\"987654321\", \"912345678\"]")
    private List<String> phone;

    @Field("email")
    @JsonProperty("email")
    @Schema(description = "Correo electrónico del cliente", example = "carlos.mendoza@agrotec.com")
    private String email;

    @Field("registration_date")
    @JsonProperty("registration_date")
    @JsonAlias({"registrationDate", "registration_date"})
    @Schema(description = "Fecha de registro", example = "2026-10-01", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate registrationDate;

    @Field("status")
    @JsonProperty("status")
    @JsonAlias({"estado", "status"})
    @Schema(description = "Estado activo/inactivo del cliente (true=activo, false=inactivo)", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean status = true;

    @Field("farm_plot")
    @JsonProperty("farm_plot")
    @JsonAlias({"farmPlot", "farm_plot"})
    @Schema(description = "Parcelas o fundos asociados al cliente")
    private List<FarmPlot> farmPlot;

    @Field("address")
    @JsonProperty("address")
    @Schema(description = "Dirección estructurada del cliente")
    private Address address;

    // Auditoría
    @Field("created_at")
    @JsonProperty("created_at")
    @JsonAlias({"createdAt", "created_at"})
    private LocalDateTime createdAt;

    @Field("update_at")
    @JsonProperty("update_at")
    @JsonAlias({"update_at", "updated_at", "updateAt", "updatedAt"})
    private LocalDateTime updateAt;

    @Field("deleted_at")
    @JsonProperty("deleted_at")
    @JsonAlias({"deletedAt", "deleted_at"})
    private LocalDateTime deletedAt;

    @Field("restored_at")
    @JsonProperty("restored_at")
    @JsonAlias({"restoredAt", "restored_at"})
    private LocalDateTime restoredAt;

    // Constructors
    public Customer() {}

    public Customer(String id, String firstName, String lastName, String documentType, String documentNumber,
                    List<String> phone, String email, LocalDate registrationDate, Boolean status,
                    List<FarmPlot> farmPlot, Address address,
                    LocalDateTime createdAt, LocalDateTime updateAt, LocalDateTime deletedAt, LocalDateTime restoredAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.phone = phone;
        this.email = email;
        this.registrationDate = registrationDate;
        this.status = status;
        this.farmPlot = farmPlot;
        this.address = address;
        this.createdAt = createdAt;
        this.updateAt = updateAt;
        this.deletedAt = deletedAt;
        this.restoredAt = restoredAt;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }

    public List<String> getPhone() { return phone; }

    @JsonSetter("phone")
    public void setPhone(List<String> phone) { this.phone = phone; }

    @JsonIgnore
    public void setPhone(String singlePhone) {
        if (singlePhone == null || singlePhone.isBlank()) {
            this.phone = Collections.emptyList();
        } else if (singlePhone.contains(",")) {
            List<String> list = new ArrayList<>();
            for (String p : singlePhone.split(",")) {
                if (!p.isBlank()) list.add(p.trim());
            }
            this.phone = list;
        } else {
            this.phone = List.of(singlePhone.trim());
        }
    }

    @JsonIgnore
    public String getPhoneFormatted() {
        if (phone == null || phone.isEmpty()) {
            return "-";
        }
        return String.join(", ", phone);
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate registrationDate) { this.registrationDate = registrationDate; }

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

    public List<FarmPlot> getFarmPlot() { return farmPlot; }
    public void setFarmPlot(List<FarmPlot> farmPlot) { this.farmPlot = farmPlot; }

    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdateAt() { return updateAt; }
    public void setUpdateAt(LocalDateTime updateAt) { this.updateAt = updateAt; }

    // Compatibilidad con getUpdatedAt / setUpdatedAt
    @JsonIgnore
    public LocalDateTime getUpdatedAt() { return updateAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updateAt = updatedAt; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public LocalDateTime getRestoredAt() { return restoredAt; }
    public void setRestoredAt(LocalDateTime restoredAt) { this.restoredAt = restoredAt; }

    // =========================================================================
    // Nested Classes for MongoDB subdocuments
    // =========================================================================

    @Schema(description = "Parcela o fundo agrícola del cliente")
    public static class FarmPlot {
        @Field("farm_name")
        @JsonProperty("farm_name")
        @JsonAlias({"farmName", "farm_name"})
        @Schema(description = "Nombre del fundo o parcela", example = "Fundo El Sol")
        private String farmName;

        @Field("hectareas")
        @JsonProperty("hectareas")
        @Schema(description = "Extensión en hectáreas", example = "12.5")
        private BigDecimal hectareas;

        @Field("type_crop")
        @JsonProperty("type_crop")
        @JsonAlias({"typeCrop", "type_crop"})
        @Schema(description = "Tipo de cultivo sembrado", example = "Uva de Mesa")
        private String typeCrop;

        public FarmPlot() {}

        public FarmPlot(String farmName, BigDecimal hectareas, String typeCrop) {
            this.farmName = farmName;
            this.hectareas = hectareas;
            this.typeCrop = typeCrop;
        }

        public String getFarmName() { return farmName; }
        public void setFarmName(String farmName) { this.farmName = farmName; }

        public BigDecimal getHectareas() { return hectareas; }
        public void setHectareas(BigDecimal hectareas) { this.hectareas = hectareas; }

        public String getTypeCrop() { return typeCrop; }
        public void setTypeCrop(String typeCrop) { this.typeCrop = typeCrop; }
    }

    @Schema(description = "Dirección estructurada del cliente")
    public static class Address {
        @Field("department")
        @JsonProperty("department")
        @Schema(description = "Departamento", example = "Ica")
        private String department;

        @Field("province")
        @JsonProperty("province")
        @Schema(description = "Provincia", example = "Ica")
        private String province;

        @Field("district")
        @JsonProperty("district")
        @Schema(description = "Distrito", example = "Subtanjalla")
        private String district;

        @Field("especifics")
        @JsonProperty("especifics")
        @Schema(description = "Detalles específicos de la dirección")
        private AddressSpecifics especifics;

        public Address() {}

        public Address(String department, String province, String district, AddressSpecifics especifics) {
            this.department = department;
            this.province = province;
            this.district = district;
            this.especifics = especifics;
        }

        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }

        public String getProvince() { return province; }
        public void setProvince(String province) { this.province = province; }

        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }

        public AddressSpecifics getEspecifics() { return especifics; }
        public void setEspecifics(AddressSpecifics especifics) { this.especifics = especifics; }
    }

    @Schema(description = "Detalles específicos de ubicación")
    public static class AddressSpecifics {
        @Field("settlement_type")
        @JsonProperty("settlement_type")
        @JsonAlias({"settlementType", "settlement_type"})
        @Schema(description = "Tipo de asentamiento (Fundo, Caserío, Sector, Anexo, etc.)", example = "Fundo")
        private String settlementType;

        @Field("settlement_name")
        @JsonProperty("settlement_name")
        @JsonAlias({"settlementName", "settlement_name"})
        @Schema(description = "Nombre del asentamiento", example = "El Sol")
        private String settlementName;

        @Field("street_type")
        @JsonProperty("street_type")
        @JsonAlias({"streetType", "street_type"})
        @Schema(description = "Tipo de vía (Carretera, Camino, Calle, Avenida, Trocha)", example = "Carretera")
        private String streetType;

        @Field("street_name")
        @JsonProperty("street_name")
        @JsonAlias({"streetName", "street_name"})
        @Schema(description = "Nombre de la vía y número", example = "Panamericana Sur Km 300")
        private String streetName;

        @Field("reference")
        @JsonProperty("reference")
        @Schema(description = "Referencia de llegada", example = "Frente a grifo Primax")
        private String reference;

        public AddressSpecifics() {}

        public AddressSpecifics(String settlementType, String settlementName, String streetType, String streetName, String reference) {
            this.settlementType = settlementType;
            this.settlementName = settlementName;
            this.streetType = streetType;
            this.streetName = streetName;
            this.reference = reference;
        }

        public String getSettlementType() { return settlementType; }
        public void setSettlementType(String settlementType) { this.settlementType = settlementType; }

        public String getSettlementName() { return settlementName; }
        public void setSettlementName(String settlementName) { this.settlementName = settlementName; }

        public String getStreetType() { return streetType; }
        public void setStreetType(String streetType) { this.streetType = streetType; }

        public String getStreetName() { return streetName; }
        public void setStreetName(String streetName) { this.streetName = streetName; }

        public String getReference() { return reference; }
        public void setReference(String reference) { this.reference = reference; }
    }
}
