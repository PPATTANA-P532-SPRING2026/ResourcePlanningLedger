package com.pm.resourceplanningledger.client;

import com.pm.resourceplanningledger.domain.ledger.AuditLogEntry;
import com.pm.resourceplanningledger.manager.LedgerManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/audit-log")
public class AuditController {

    private final LedgerManager ledgerManager;

    public AuditController(LedgerManager ledgerManager) {
        this.ledgerManager = ledgerManager;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list() {
        List<AuditLogEntry> entries = ledgerManager.getAuditLog();
        List<Map<String, Object>> result = new ArrayList<>();
        for (AuditLogEntry e : entries) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", e.getId());
            map.put("event", e.getEvent());
            map.put("accountId", e.getAccountId());
            map.put("entryId", e.getEntryId());
            map.put("actionId", e.getActionId());
            map.put("timestamp", e.getTimestamp());
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }
}