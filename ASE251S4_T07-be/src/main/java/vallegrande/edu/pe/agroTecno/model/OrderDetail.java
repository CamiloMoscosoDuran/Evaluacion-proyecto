package vallegrande.edu.pe.agroTecno.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(value = "order_detail", schema = "ventas")
public class OrderDetail {

    @Id
    @Column("order_detail_id")
    private Integer orderDetailId;

    @Column("sales_order_order_id")
    private Integer salesOrderOrderId;

    @Column("mongo_formula_id")
    private String mongoFormulaId;

    @Column("quantity")
    private BigDecimal quantity;

    @Column("unit_price")
    private BigDecimal unitPrice;

    // subtotal is calculated by SQL Server and must not be written from the backend
    @ReadOnlyProperty
    @Column("subtotal")
    private BigDecimal subtotal;

    @Column("created_at")
    private LocalDateTime createdAt;

    public OrderDetail() {}

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
