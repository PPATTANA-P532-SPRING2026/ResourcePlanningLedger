package com.pm.resourceplanningledger.dto;

import java.util.List;

public class ActionDTO {
    private String name;
    private String party;
    private String location;
    private List<String> dependsOn;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getParty() { return party; }
    public void setParty(String party) { this.party = party; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public List<String> getDependsOn() { return dependsOn; }
    public void setDependsOn(List<String> dependsOn) { this.dependsOn = dependsOn; }
}