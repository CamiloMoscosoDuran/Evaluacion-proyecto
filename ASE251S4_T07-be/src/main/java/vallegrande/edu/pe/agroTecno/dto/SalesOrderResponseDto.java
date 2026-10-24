package vallegrande.edu.pe.agroTecno.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import vallegrande.edu.pe.agroTecno.model.Customer;
import vallegrande.edu.pe.agroTecno.model.PaymentCollection;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Respuesta de orden de venta con su información de cliente y detalles de productos")
public class SalesOrderResponseDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Integer orderId;

    @Schema(description = "Guía de remisión", example = "GR-001")
    private String referralGuide;

    @Schema(description = "ID del cliente en MongoDB", example = "CUS003")
    private String mongoCustomerId;

    @Schema(description = "Datos completos del cliente desde MongoDB")
    private Customer customerInfo;

    @Schema(description = "Fecha de registro de la orden")
    private LocalDateTime entryDate;

    @Schema(description = "Monto total de la orden", example = "1500.00")
    private BigDecimal totalOrder;

    @Schema(description = "Estado de la orden", example = "A")
    private String orderStatus;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, nullable = true)
    private LocalDateTime updatedAt;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, nullable = true)
    private LocalDateTime deletedAt;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, nullable = true)
    private LocalDateTime restoredAt;

    @Schema(description = "Detalles de productos asociados a la orden")
    private List<OrderDetailDto> orderDetails;
    private PaymentCollection paymentCollection;

    public SalesOrderResponseDto() {}

    public SalesOrderResponseDto(Integer orderId, String referralGuide, String mongoCustomerId, Customer customerInfo, LocalDateTime entryDate, BigDecimal totalOrder, String orderStatus, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt, LocalDateTime restoredAt, List<OrderDetailDto> orderDetails) {
        this.orderId = orderId;
        this.referralGuide = referralGuide;
        this.mongoCustomerId = mongoCustomerId;
        this.customerInfo = customerInfo;
        this.entryDate = entryDate;
        this.totalOrder = totalOrder;
        this.orderStatus = orderStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.restoredAt = restoredAt;
        this.orderDetails = orderDetails;
    }

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }

    public String getReferralGuide() { return referralGuide; }
    public void setReferralGuide(String referralGuide) { this.referralGuide = referralGuide; }

    public String getMongoCustomerId() { return mongoCustomerId; }
    public void setMongoCustomerId(String mongoCustomerId) { this.mongoCustomerId = mongoCustomerId; }

    public Customer getCustomerInfo() { return customerInfo; }
    public void setCustomerInfo(Customer customerInfo) { this.customerInfo = customerInfo; }

    public LocalDateTime getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDateTime entryDate) { this.entryDate = entryDate; }

    public BigDecimal getTotalOrder() { return totalOrder; }
    public void setTotalOrder(BigDecimal totalOrder) { this.totalOrder = totalOrder; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public LocalDateTime getRestoredAt() { return restoredAt; }
    public void setRestoredAt(LocalDateTime restoredAt) { this.restoredAt = restoredAt; }

    public List<OrderDetailDto> getOrderDetails() { return orderDetails; }
    public void setOrderDetails(List<OrderDetailDto> orderDetails) { this.orderDetails = orderDetails; }

    public PaymentCollection getPaymentCollection() { return paymentCollection; }
    public void setPaymentCollection(PaymentCollection paymentCollection) { this.paymentCollection = paymentCollection; }
}
