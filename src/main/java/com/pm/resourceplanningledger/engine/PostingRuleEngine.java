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

        // collect new entries separately to avoid modifying list while iterating
        List<Entry> newEntries = new ArrayList<>();

        for (Entry entry : transaction.getEntries()) {

            Account account = entry.getAccount();

            // only apply to pool accounts
            if (account == null || account.getKind() != Account.AccountKind.POOL) {
                continue;
            }

            List<PostingRule> rules = postingRuleRepository.findByTriggerAccount(account);

            for (PostingRule rule : rules) {

                BigDecimal balance = entryRepository.getAccountBalance(account.getId());

                // trigger condition: pool goes below zero
                if (balance != null && balance.compareTo(BigDecimal.ZERO) < 0) {

                    // safety check
                    if (rule.getOutputAccount() == null) {
                        continue;
                    }

                    LocalDateTime now = LocalDateTime.now(clock);

                    Entry alertEntry = new Entry();

                    // 🔥 CRITICAL: attach to transaction
                    alertEntry.setTransaction(transaction);

                    alertEntry.setAccount(rule.getOutputAccount());

                    // alert is a signal → not the full negative balance
                    alertEntry.setAmount(BigDecimal.ONE);

                    alertEntry.setChargedAt(now);
                    alertEntry.setBookedAt(now);

                    // link to originating action
                    alertEntry.setActionId(entry.getActionId());

                    // DO NOT save directly → let transaction persist it
                    newEntries.add(alertEntry);
                }
            }
        }

        // add after iteration to avoid ConcurrentModificationException
        for (Entry e : newEntries) {
            transaction.addEntry(e);
        }
    }
}