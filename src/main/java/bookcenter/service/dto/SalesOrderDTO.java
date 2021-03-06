package bookcenter.service.dto;


import java.time.Instant;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A DTO for the SalesOrder entity.
 */
public class SalesOrderDTO implements Serializable {

    private Long id;

    @NotNull
    private Instant date;

    private String customer;

    @NotNull
    private BigDecimal totalAmount;

    private Long warehouseId;

    private String warehouseName;

    private Long sellerId;

    private String sellerName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long employeeId) {
        this.sellerId = employeeId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SalesOrderDTO salesOrderDTO = (SalesOrderDTO) o;
        if(salesOrderDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), salesOrderDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "SalesOrderDTO{" +
            "id=" + getId() +
            ", date='" + getDate() + "'" +
            ", customer='" + getCustomer() + "'" +
            ", totalAmount=" + getTotalAmount() +
            "}";
    }
}
