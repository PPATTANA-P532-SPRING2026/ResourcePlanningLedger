package com.pm.resourceplanningledger.dto;

public class PlanDTO {
    private String name;
    private Long protocolId;
    private String targetStartDate;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getProtocolId() { return protocolId; }
    public void setProtocolId(Long protocolId) { this.protocolId = protocolId; }

    public String getTargetStartDate() { return targetStartDate; }
    public void setTargetStartDate(String targetStartDate) { this.targetStartDate = targetStartDate; }
}