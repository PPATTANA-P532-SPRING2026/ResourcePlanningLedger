package com.pm.resourceplanningledger.manager;

import com.pm.resourceplanningledger.domain.ledger.Account;
import com.pm.resourceplanningledger.domain.ledger.AuditLogEntry;
import com.pm.resourceplanningledger.domain.ledger.Entry;
import com.pm.resourceplanningledger.domain.ledger.PostingRule;
import com.pm.resourceplanningledger.resourceaccess.AccountRepository;
import com.pm.resourceplanningledger.resourceaccess.AuditLogRepository;
import com.pm.resourceplanningledger.resourceaccess.EntryRepository;
import com.pm.resourceplanningledger.resourceaccess.PostingRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class LedgerManager {

    private final AccountRepository accountRepository;
    private final EntryRepository entryRepository;
    private final PostingRuleRepository postingRuleRepository;
    private final AuditLogRepository auditLogRepository;

    public LedgerManager(AccountRepository accountRepository,
                         EntryRepository entryRepository,
                         PostingRuleRepository postingRuleRepository,
                         AuditLogRepository auditLogRepository) {
        this.accountRepository = accountRepository;
        this.entryRepository = entryRepository;
        this.postingRuleRepository = postingRuleRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public List<Account> findAllAccounts() {
        return accountRepository.findAll();
    }

    public Account findAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
    }

    public List<Entry> getEntriesForAccount(Long accountId) {
        return entryRepository.findByAccountIdOrderByBookedAtDesc(accountId);
    }

    public BigDecimal getAccountBalance(Long accountId) {
        return entryRepository.getAccountBalance(accountId);
    }

    public List<Map<String, Object>> getAllAccountsWithBalances() {
        List<Account> accounts = accountRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Account account : accounts) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", account.getId());
            entry.put("name", account.getName());
            entry.put("kind", account.getKind().name());
            entry.put("balance", getAccountBalance(account.getId()));
            if (account.getResourceType() != null) {
                entry.put("resourceTypeName", account.getResourceType().getName());
                entry.put("resourceTypeKind", account.getResourceType().getKind().name());
            }
            result.add(entry);
        }
        return result;
    }

    public List<AuditLogEntry> getAuditLog() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }

    @Transactional
    public PostingRule createPostingRule(Long triggerAccountId, Long outputAccountId, String strategyType) {
        Account trigger = findAccountById(triggerAccountId);
        Account output = findAccountById(outputAccountId);
        PostingRule rule = new PostingRule(trigger, output, strategyType);
        return postingRuleRepository.save(rule);
    }

    public List<PostingRule> getAllPostingRules() {
        return postingRuleRepository.findAll();
    }
}