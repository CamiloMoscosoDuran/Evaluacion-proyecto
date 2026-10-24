package vallegrande.edu.pe.agroTecno.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderDetailDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Integer orderDetailId;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Integer salesOrderOrderId;
    private String mongoFormulaId;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal subtotal;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    public OrderDetailDto() {}

    public OrderDetailDto(Integer orderDetailId, Integer salesOrderOrderId, String mongoFormulaId,
                          BigDecimal quantity, BigDecimal unitPrice, BigDecimal subtotal,
                          LocalDateTime createdAt) {
        this.orderDetailId = orderDetailId;
        this.salesOrderOrderId = salesOrderOrderId;
        this.mongoFormulaId = mongoFormulaId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
        this.createdAt = createdAt;
    }

    public Integer getOrderDetailId() { return orderDetailId; }
    public void setOrderDetailId(Integer orderDetailId) { this.orderDetailId = orderDetailId; }

    public Integer getSalesOrderOrderId() { return salesOrderOrderId; }
    public void setSalesOrderOrderId(Integer salesOrderOrderId) { this.salesOrderOrderId = salesOrderOrderId; }

    public String getMongoFormulaId() { return mongoFormulaId; }
    public void setMongoFormulaId(String mongoFormulaId) { this.mongoFormulaId = mongoFormulaId; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
