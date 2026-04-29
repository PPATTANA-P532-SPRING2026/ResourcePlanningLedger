package com.pm.resourceplanningledger.domain.knowledge;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "protocol_steps")
public class ProtocolStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protocol_id", nullable = false)
    private Protocol protocol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_protocol_id")
    private Protocol subProtocol;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "step_dependencies", joinColumns = @JoinColumn(name = "step_id"))
    @Column(name = "depends_on_step_name")
    private List<String> dependsOn = new ArrayList<>();

    public ProtocolStep() {}

    public ProtocolStep(String name) {
        this.name = name;
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Protocol getProtocol() { return protocol; }
    public void setProtocol(Protocol protocol) { this.protocol = protocol; }

    public Protocol getSubProtocol() { return subProtocol; }
    public void setSubProtocol(Protocol subProtocol) { this.subProtocol = subProtocol; }

    public List<String> getDependsOn() { return dependsOn; }
    public void setDependsOn(List<String> dependsOn) { this.dependsOn = dependsOn; }
}