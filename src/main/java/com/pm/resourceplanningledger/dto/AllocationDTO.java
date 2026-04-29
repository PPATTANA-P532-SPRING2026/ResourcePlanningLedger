package com.pm.resourceplanningledger.dto;

import java.math.BigDecimal;

public class AllocationDTO {
    private Long resourceTypeId;
    private BigDecimal quantity;
    private String kind;  // GENERAL or SPECIFIC
    private String assetId;
    private String timePeriod;

    public Long getResourceTypeId() { return resourceTypeId; }
    public void setResourceTypeId(Long resourceTypeId) { this.resourceTypeId = resourceTypeId; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }

    public String getAssetId() { return assetId; }
    public void setAssetId(String assetId) { this.assetId = assetId; }

    public String getTimePeriod() { return timePeriod; }
    public void setTimePeriod(String timePeriod) { this.timePeriod = timePeriod; }
}