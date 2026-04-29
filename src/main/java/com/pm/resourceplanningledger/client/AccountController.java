package com.pm.resourceplanningledger.client;

import com.pm.resourceplanningledger.domain.ledger.Entry;
import com.pm.resourceplanningledger.manager.LedgerManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final LedgerManager ledgerManager;

    public AccountController(LedgerManager ledgerManager) {
        this.ledgerManager = ledgerManager;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list() {
        return ResponseEntity.ok(ledgerManager.getAllAccountsWithBalances());
    }

    @GetMapping("/{id}/entries")
    public ResponseEntity<List<Map<String, Object>>> entries(@PathVariable Long id) {
        List<Entry> entries = ledgerManager.getEntriesForAccount(id);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Entry e : entries) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", e.getId());
            map.put("amount", e.getAmount());
            map.put("chargedAt", e.getChargedAt());
            map.put("bookedAt", e.getBookedAt());
            map.put("actionId", e.getActionId());
            if (e.getTransaction() != null) {
                map.put("transactionId", e.getTransaction().getId());
                map.put("transactionDescription", e.getTransaction().getDescription());
            }
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }
}