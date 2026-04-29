package com.pm.resourceplanningledger.domain.knowledge;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "protocols")
public class Protocol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @OneToMany(mappedBy = "protocol", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderColumn(name = "step_order")
    private List<ProtocolStep> steps = new ArrayList<>();

    public Protocol() {}

    public Protocol(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<ProtocolStep> getSteps() { return steps; }
    public void setSteps(List<ProtocolStep> steps) { this.steps = steps; }

    public void addStep(ProtocolStep step) {
        steps.add(step);
        step.setProtocol(this);
    }
}