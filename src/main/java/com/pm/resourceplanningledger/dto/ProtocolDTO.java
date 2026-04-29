package com.pm.resourceplanningledger.dto;

import java.util.List;

public class ProtocolDTO {
    private Long id;
    private String name;
    private String description;
    private List<ProtocolStepDTO> steps;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<ProtocolStepDTO> getSteps() { return steps; }
    public void setSteps(List<ProtocolStepDTO> steps) { this.steps = steps; }

    public static class ProtocolStepDTO {
        private Long id;
        private String name;
        private Long subProtocolId;
        private List<String> dependsOn;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Long getSubProtocolId() { return subProtocolId; }
        public void setSubProtocolId(Long subProtocolId) { this.subProtocolId = subProtocolId; }

        public List<String> getDependsOn() { return dependsOn; }
        public void setDependsOn(List<String> dependsOn) { this.dependsOn = dependsOn; }
    }
}