package com.pm.resourceplanningledger.domain.operational;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "suspensions")
public class Suspension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposed_action_id", nullable = false)
    private ProposedAction proposedAction;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    public Suspension() {}

    public Suspension(ProposedAction proposedAction, String reason, LocalDate startDate) {
        this.proposedAction = proposedAction;
        this.reason = reason;
        this.startDate = startDate;
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ProposedAction getProposedAction() { return proposedAction; }
    public void setProposedAction(ProposedAction proposedAction) { this.proposedAction = proposedAction; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}