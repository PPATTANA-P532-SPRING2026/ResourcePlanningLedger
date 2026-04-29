package com.pm.resourceplanningledger.domain.ledger;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
public class AuditLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String event;

    private Long accountId;
    private Long entryId;
    private Long actionId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public AuditLogEntry() {}

    public AuditLogEntry(String event, Long accountId, Long entryId, Long actionId, LocalDateTime timestamp) {
        this.event = event;
        this.accountId = accountId;
        this.entryId = entryId;
        this.actionId = actionId;
        this.timestamp = timestamp;
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public Long getEntryId() { return entryId; }
    public void setEntryId(Long entryId) { this.entryId = entryId; }

    public Long getActionId() { return actionId; }
    public void setActionId(Long actionId) { this.actionId = actionId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}