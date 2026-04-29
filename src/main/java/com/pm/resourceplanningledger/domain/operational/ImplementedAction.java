package com.pm.resourceplanningledger.domain.operational;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "implemented_actions")
public class ImplementedAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposed_action_id", nullable = false, unique = true)
    private ProposedAction proposedAction;

    @Column(nullable = false)
    private LocalDateTime actualStart;

    private String actualParty;
    private String actualLocation;
    private String status;

    public ImplementedAction() {}

    public ImplementedAction(ProposedAction proposedAction, LocalDateTime actualStart) {
        this.proposedAction = proposedAction;
        this.actualStart = actualStart;
        this.status = "IN_PROGRESS";
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ProposedAction getProposedAction() { return proposedAction; }
    public void setProposedAction(ProposedAction proposedAction) { this.proposedAction = proposedAction; }

    public LocalDateTime getActualStart() { return actualStart; }
    public void setActualStart(LocalDateTime actualStart) { this.actualStart = actualStart; }

    public String getActualParty() { return actualParty; }
    public void setActualParty(String actualParty) { this.actualParty = actualParty; }

    public String getActualLocation() { return actualLocation; }
    public void setActualLocation(String actualLocation) { this.actualLocation = actualLocation; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}