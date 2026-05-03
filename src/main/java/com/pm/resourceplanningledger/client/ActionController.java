package com.pm.resourceplanningledger.client;

import com.pm.resourceplanningledger.domain.operational.ProposedAction;
import com.pm.resourceplanningledger.domain.operational.ResourceAllocation;
import com.pm.resourceplanningledger.dto.AllocationDTO;
import com.pm.resourceplanningledger.dto.SuspendDTO;
import com.pm.resourceplanningledger.manager.ActionManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/actions")
public class ActionController {

    private final ActionManager actionManager;

    public ActionController(ActionManager actionManager) {
        this.actionManager = actionManager;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable Long id) {
        ProposedAction action = actionManager.findById(id);
        return ResponseEntity.ok(toMap(action));
    }

    @PostMapping("/{id}/implement")
    public ResponseEntity<Map<String, Object>> implement(@PathVariable Long id,
                                                         @RequestBody(required = false) Map<String, String> body) {
        String actualParty = null;
        String actualLocation = null;
        if (body != null) {
            actualParty = body.get("actualParty");
            actualLocation = body.get("actualLocation");
        }
        ProposedAction action = actionManager.implement(id, actualParty, actualLocation);
        return ResponseEntity.ok(toMap(action));
    }

    @PostMapping("/{id}/submit-for-approval")
    public ResponseEntity<Map<String, Object>> submitForApproval(@PathVariable Long id) {
        ProposedAction action = actionManager.submitForApproval(id);
        return ResponseEntity.ok(toMap(action));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Map<String, Object>> approve(@PathVariable Long id,
                                                       @RequestBody(required = false) Map<String, String> body) {
        String actualParty = null;
        String actualLocation = null;
        if (body != null) {
            actualParty = body.get("actualParty");
            actualLocation = body.get("actualLocation");
        }
        ProposedAction action = actionManager.approve(id, actualParty, actualLocation);
        return ResponseEntity.ok(toMap(action));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> reject(@PathVariable Long id) {
        ProposedAction action = actionManager.reject(id);
        return ResponseEntity.ok(toMap(action));
    }

    @PostMapping("/{id}/reopen")
    public ResponseEntity<Map<String, Object>> reopen(@PathVariable Long id) {
        ProposedAction action = actionManager.reopen(id);
        return ResponseEntity.ok(toMap(action));
    }

    @PostMapping("/{id}/suspend")
    public ResponseEntity<Map<String, Object>> suspend(@PathVariable Long id, @RequestBody SuspendDTO dto) {
        ProposedAction action = actionManager.suspend(id, dto.getReason());
        return ResponseEntity.ok(toMap(action));
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<Map<String, Object>> resume(@PathVariable Long id) {
        ProposedAction action = actionManager.resume(id);
        return ResponseEntity.ok(toMap(action));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Map<String, Object>> complete(@PathVariable Long id) {
        ProposedAction action = actionManager.complete(id);
        return ResponseEntity.ok(toMap(action));
    }

    @PostMapping("/{id}/abandon")
    public ResponseEntity<Map<String, Object>> abandon(@PathVariable Long id) {
        ProposedAction action = actionManager.abandon(id);
        return ResponseEntity.ok(toMap(action));
    }

    @PostMapping("/{id}/allocations")
    public ResponseEntity<Map<String, Object>> addAllocation(@PathVariable Long id, @RequestBody AllocationDTO dto) {
        ResourceAllocation alloc = actionManager.addAllocation(id, dto.getResourceTypeId(), dto.getQuantity(),
                ResourceAllocation.AllocationKind.valueOf(dto.getKind()), dto.getAssetId(), dto.getTimePeriod());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", alloc.getId());
        result.put("resourceTypeName", alloc.getResourceType().getName());
        result.put("quantity", alloc.getQuantity());
        result.put("kind", alloc.getKind().name());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/implemented")
    public ResponseEntity<Map<String, Object>> updateImplemented(@PathVariable Long id,
                                                                 @RequestBody Map<String, String> body) {
        ProposedAction action = actionManager.updateImplementedAction(id,
                body.get("actualParty"), body.get("actualLocation"));
        return ResponseEntity.ok(toMap(action));
    }

    private Map<String, Object> toMap(ProposedAction action) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", action.getId());
        map.put("name", action.getName());
        map.put("state", action.getStateName());
        map.put("party", action.getParty());
        map.put("location", action.getLocation());
        map.put("dependsOn", action.getDependsOn());

        List<String> legalTransitions = new ArrayList<>();
        switch (action.getStateName()) {
            case "PROPOSED" -> { legalTransitions.add("submitForApproval"); legalTransitions.add("suspend"); legalTransitions.add("abandon"); }
            case "PENDING_APPROVAL" -> { legalTransitions.add("approve"); legalTransitions.add("reject"); }
            case "SUSPENDED" -> { legalTransitions.add("resume"); legalTransitions.add("abandon"); }
            case "IN_PROGRESS" -> { legalTransitions.add("complete"); legalTransitions.add("suspend"); legalTransitions.add("abandon"); }
            case "COMPLETED" -> { legalTransitions.add("reopen"); }
            case "REOPENED" -> { legalTransitions.add("complete"); legalTransitions.add("abandon"); }
        }
        map.put("legalTransitions", legalTransitions);

        List<Map<String, Object>> allocations = new ArrayList<>();
        for (var alloc : action.getAllocations()) {
            Map<String, Object> allocMap = new LinkedHashMap<>();
            allocMap.put("id", alloc.getId());
            allocMap.put("resourceTypeName", alloc.getResourceType().getName());
            allocMap.put("resourceTypeId", alloc.getResourceType().getId());
            allocMap.put("quantity", alloc.getQuantity());
            allocMap.put("kind", alloc.getKind().name());
            allocMap.put("assetId", alloc.getAssetId());
            allocMap.put("timePeriod", alloc.getTimePeriod());
            allocations.add(allocMap);
        }
        map.put("allocations", allocations);

        if (action.getImplementedAction() != null) {
            var impl = action.getImplementedAction();
            Map<String, Object> implMap = new LinkedHashMap<>();
            implMap.put("id", impl.getId());
            implMap.put("actualStart", impl.getActualStart());
            implMap.put("actualParty", impl.getActualParty());
            implMap.put("actualLocation", impl.getActualLocation());
            implMap.put("status", impl.getStatus());
            Map<String, Object> diff = new LinkedHashMap<>();
            if (!Objects.equals(action.getParty(), impl.getActualParty())) {
                diff.put("party", Map.of("proposed", nullSafe(action.getParty()), "actual", nullSafe(impl.getActualParty())));
            }
            if (!Objects.equals(action.getLocation(), impl.getActualLocation())) {
                diff.put("location", Map.of("proposed", nullSafe(action.getLocation()), "actual", nullSafe(impl.getActualLocation())));
            }
            implMap.put("diff", diff);
            map.put("implementedAction", implMap);
        }

        List<Map<String, Object>> suspensions = new ArrayList<>();
        for (var s : action.getSuspensions()) {
            Map<String, Object> sMap = new LinkedHashMap<>();
            sMap.put("id", s.getId());
            sMap.put("reason", s.getReason());
            sMap.put("startDate", s.getStartDate());
            sMap.put("endDate", s.getEndDate());
            suspensions.add(sMap);
        }
        map.put("suspensions", suspensions);

        return map;
    }

    private String nullSafe(String s) { return s == null ? "" : s; }
}