package com.pm.resourceplanningledger.domain.ledger;

import jakarta.persistence.*;

@Entity
@Table(name = "posting_rules")
public class PostingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "trigger_account_id", nullable = false)
    private Account triggerAccount;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "output_account_id", nullable = false)
    private Account outputAccount;

    @Column(nullable = false)
    private String strategyType;

    public PostingRule() {}

    public PostingRule(Account triggerAccount, Account outputAccount, String strategyType) {
        this.triggerAccount = triggerAccount;
        this.outputAccount = outputAccount;
        this.strategyType = strategyType;
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Account getTriggerAccount() { return triggerAccount; }
    public void setTriggerAccount(Account triggerAccount) { this.triggerAccount = triggerAccount; }

    public Account getOutputAccount() { return outputAccount; }
    public void setOutputAccount(Account outputAccount) { this.outputAccount = outputAccount; }

    public String getStrategyType() { return strategyType; }
    public void setStrategyType(String strategyType) { this.strategyType = strategyType; }
}