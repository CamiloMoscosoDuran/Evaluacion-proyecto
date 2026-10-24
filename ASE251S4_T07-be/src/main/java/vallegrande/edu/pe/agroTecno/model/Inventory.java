package vallegrande.edu.pe.agroTecno.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;

@Schema(description = "Existencias y fecha de caducidad del insumo / suministro")
public class Inventory {

    @Field("current_stock")
    @JsonProperty("current_stock")
    @JsonAlias({"current_stock", "currentStock"})
    @Schema(example = "100.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double current_stock;

    @Field("minimum_stock")
    @JsonProperty("minimum_stock")
    @JsonAlias({"minimum_stock", "minimumStock"})
    @Schema(example = "10.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double minimum_stock;

    @Field("expiration_date")
    @JsonProperty("expiration_date")
    @JsonAlias({"expiration_date", "expirationDate"})
    @Schema(type = "string", format = "date", example = "2027-12-31", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate expiration_date;

    public Inventory() {}

    public Inventory(Double current_stock, Double minimum_stock, LocalDate expiration_date) {
        this.current_stock = current_stock;
        this.minimum_stock = minimum_stock;
        this.expiration_date = expiration_date;
    }

    // Snake case getters & setters
    public Double getCurrent_stock() { return current_stock; }
    public void setCurrent_stock(Double current_stock) { this.current_stock = current_stock; }

    public Double getMinimum_stock() { return minimum_stock; }
    public void setMinimum_stock(Double minimum_stock) { this.minimum_stock = minimum_stock; }

    public LocalDate getExpiration_date() { return expiration_date; }
    public void setExpiration_date(LocalDate expiration_date) { this.expiration_date = expiration_date; }

    // Camel case getters & setters for convenience
    public Double getCurrentStock() { return current_stock; }
    public void setCurrentStock(Double currentStock) { this.current_stock = currentStock; }

    public Double getMinimumStock() { return minimum_stock; }
    public void setMinimumStock(Double minimumStock) { this.minimum_stock = minimumStock; }

    public LocalDate getExpirationDate() { return expiration_date; }
    public void setExpirationDate(LocalDate expirationDate) { this.expiration_date = expirationDate; }
}
