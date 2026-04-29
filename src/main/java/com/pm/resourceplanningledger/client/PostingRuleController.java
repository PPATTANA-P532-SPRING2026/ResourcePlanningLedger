package com.pm.resourceplanningledger.client;

import com.pm.resourceplanningledger.domain.ledger.PostingRule;
import com.pm.resourceplanningledger.manager.LedgerManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/posting-rules")
public class PostingRuleController {

    private final LedgerManager ledgerManager;

    public PostingRuleController(LedgerManager ledgerManager) {
        this.ledgerManager = ledgerManager;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list() {
        List<PostingRule> rules = ledgerManager.getAllPostingRules();
        List<Map<String, Object>> result = new ArrayList<>();
        for (PostingRule r : rules) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", r.getId());
            map.put("triggerAccountId", r.getTriggerAccount().getId());
            map.put("triggerAccountName", r.getTriggerAccount().getName());
            map.put("outputAccountId", r.getOutputAccount().getId());
            map.put("outputAccountName", r.getOutputAccount().getName());
            map.put("strategyType", r.getStrategyType());
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        Long triggerAccountId = Long.valueOf(body.get("triggerAccountId").toString());
        Long outputAccountId = Long.valueOf(body.get("outputAccountId").toString());
        String strategyType = body.get("strategyType").toString();

        PostingRule rule = ledgerManager.createPostingRule(triggerAccountId, outputAccountId, strategyType);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", rule.getId());
        map.put("triggerAccountId", rule.getTriggerAccount().getId());
        map.put("outputAccountId", rule.getOutputAccount().getId());
        map.put("strategyType", rule.getStrategyType());
        return ResponseEntity.ok(map);
    }
}