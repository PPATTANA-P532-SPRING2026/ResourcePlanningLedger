package com.pm.resourceplanningledger.engine;

import com.pm.resourceplanningledger.domain.ledger.*;
import com.pm.resourceplanningledger.resourceaccess.EntryRepository;
import com.pm.resourceplanningledger.resourceaccess.PostingRuleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
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
        for (Entry entry : transaction.getEntries()) {
            Account account = entry.getAccount();
            if (account == null || account.getKind() != Account.AccountKind.POOL) continue;

            List<PostingRule> rules = postingRuleRepository.findByTriggerAccount(account);
            for (PostingRule rule : rules) {
                BigDecimal balance = entryRepository.getAccountBalance(account.getId());
                if (balance != null && balance.compareTo(BigDecimal.ZERO) < 0) {
                    // Create alert entry on the alert memo account
                    LocalDateTime now = LocalDateTime.now(clock);
                    Entry alertEntry = new Entry(
                            rule.getOutputAccount(),
                            balance,
                            now,
                            now
                    );
                    transaction.addEntry(alertEntry);
                }
            }
        }
    }
}