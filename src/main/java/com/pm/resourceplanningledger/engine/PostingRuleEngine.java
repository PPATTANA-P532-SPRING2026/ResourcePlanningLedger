package com.pm.resourceplanningledger.engine;

import com.pm.resourceplanningledger.domain.ledger.*;
import com.pm.resourceplanningledger.resourceaccess.EntryRepository;
import com.pm.resourceplanningledger.resourceaccess.PostingRuleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PostingRuleEngine {

    private final PostingRuleRepository postingRuleRepository;
    private final EntryRepository entryRepository;
    private final Clock clock;

    public PostingRuleEngine(PostingRuleRepository postingRuleRepository,
                             EntryRepository entryRepository,
                             Clock clock) {
        this.postingRuleRepository = postingRuleRepository;
        this.entryRepository = entryRepository;
        this.clock = clock;
    }

    public void evaluate(Transaction transaction) {

        // Collect new entries separately to avoid ConcurrentModificationException
        List<Entry> newEntries = new ArrayList<>();

        for (Entry entry : transaction.getEntries()) {
            Account account = entry.getAccount();

            // Only apply rules to pool accounts
            if (account == null || account.getKind() != Account.AccountKind.POOL) {
                continue;
            }

            List<PostingRule> rules = postingRuleRepository.findByTriggerAccount(account);

            for (PostingRule rule : rules) {

                BigDecimal balance = entryRepository.getAccountBalance(account.getId());

                if (balance != null && balance.compareTo(BigDecimal.ZERO) < 0) {

                    // Safety check: ensure output account exists
                    if (rule.getOutputAccount() == null) {
                        continue;
                    }

                    LocalDateTime now = LocalDateTime.now(clock);

                    Entry alertEntry = new Entry();
                    alertEntry.setAccount(rule.getOutputAccount());

                    // Use a simple positive alert signal (not the full negative balance)
                    alertEntry.setAmount(BigDecimal.ONE);

                    alertEntry.setChargedAt(now);
                    alertEntry.setBookedAt(now);

                    // Link alert to the same action for traceability
                    alertEntry.setActionId(entry.getActionId());

                    // Persist immediately
                    entryRepository.save(alertEntry);

                    // Add to transaction after loop
                    newEntries.add(alertEntry);
                }
            }
        }

        // Add new entries after iteration (safe)
        for (Entry e : newEntries) {
            transaction.addEntry(e);
        }
    }
}