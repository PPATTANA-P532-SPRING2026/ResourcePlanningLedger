package com.pm.resourceplanningledger.dto;

public class ResourceTypeDTO {
    private Long id;
    private String name;
    private String kind;  // ASSET or CONSUMABLE
    private String unit;
    private Long poolAccountId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public Long getPoolAccountId() { return poolAccountId; }
    public void setPoolAccountId(Long poolAccountId) { this.poolAccountId = poolAccountId; }
}