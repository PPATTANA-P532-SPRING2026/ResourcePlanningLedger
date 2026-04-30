package com.pm.resourceplanningledger.manager;

import com.pm.resourceplanningledger.domain.knowledge.ResourceType;
import com.pm.resourceplanningledger.domain.ledger.Account;
import com.pm.resourceplanningledger.domain.ledger.Entry;
import com.pm.resourceplanningledger.domain.ledger.PostingRule;
import com.pm.resourceplanningledger.domain.ledger.Transaction;
import com.pm.resourceplanningledger.resourceaccess.AccountRepository;
import com.pm.resourceplanningledger.resourceaccess.PostingRuleRepository;
import com.pm.resourceplanningledger.resourceaccess.ResourceTypeRepository;
import com.pm.resourceplanningledger.resourceaccess.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ResourceTypeManager {

    private final ResourceTypeRepository resourceTypeRepository;
    private final AccountRepository accountRepository;
    private final PostingRuleRepository postingRuleRepository;
    private final TransactionRepository transactionRepository;

    public ResourceTypeManager(ResourceTypeRepository resourceTypeRepository,
                               AccountRepository accountRepository,
                               PostingRuleRepository postingRuleRepository,
                               TransactionRepository transactionRepository) {
        this.resourceTypeRepository = resourceTypeRepository;
        this.accountRepository = accountRepository;
        this.postingRuleRepository = postingRuleRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<ResourceType> findAll() {
        return resourceTypeRepository.findAll();
    }

    public ResourceType findById(Long id) {
        return resourceTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ResourceType not found: " + id));
    }

    @Transactional
    public ResourceType create(ResourceType resourceType, double initialBalance) {
        // Auto-create pool account
        Account poolAccount = new Account(
                "Pool - " + resourceType.getName(),
                Account.AccountKind.POOL,
                resourceType
        );
        poolAccount = accountRepository.save(poolAccount);
        resourceType.setPoolAccount(poolAccount);

        // Auto-create alert memo account
        Account alertAccount = new Account(
                "Alert - " + resourceType.getName(),
                Account.AccountKind.ALERT_MEMO,
                resourceType
        );
        alertAccount = accountRepository.save(alertAccount);

        // Auto-create posting rule: pool → alert memo
        PostingRule rule = new PostingRule(poolAccount, alertAccount, "OVER_CONSUMPTION_ALERT");
        postingRuleRepository.save(rule);

        // Save resource type first so pool account FK is set
        ResourceType saved = resourceTypeRepository.save(resourceType);

        // Deposit initial balance into pool account as an opening entry
        if (initialBalance > 0) {
            LocalDateTime now = LocalDateTime.now();
            Transaction tx = new Transaction("Opening balance - " + resourceType.getName(), now);
            Entry entry = new Entry(poolAccount, BigDecimal.valueOf(initialBalance), now, now);
            tx.addEntry(entry);
            transactionRepository.save(tx);
        }

        return saved;
    }

    @Transactional
    public ResourceType update(Long id, ResourceType updated) {
        ResourceType existing = findById(id);
        existing.setName(updated.getName());
        existing.setKind(updated.getKind());
        existing.setUnit(updated.getUnit());
        return resourceTypeRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        resourceTypeRepository.deleteById(id);
    }
}