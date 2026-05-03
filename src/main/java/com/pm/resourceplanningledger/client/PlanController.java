package com.pm.resourceplanningledger.client;

import com.pm.resourceplanningledger.domain.operational.*;
import com.pm.resourceplanningledger.dto.ActionDTO;
import com.pm.resourceplanningledger.dto.PlanDTO;
import com.pm.resourceplanningledger.manager.PlanManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/plans")
public class PlanController {

    private final PlanManager planManager;

    public PlanController(PlanManager planManager) {
        this.planManager = planManager;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list() {
        List<Plan> plans = planManager.findAllTopLevel();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Plan p : plans) result.add(toMapShallow(p));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable Long id) {
        return ResponseEntity.ok(toMapDeep(planManager.findById(id)));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody PlanDTO dto) {
        Plan plan;
        if (dto.getProtocolId() != null) {
            plan = planManager.createFromProtocol(dto.getProtocolId(), dto.getName());
        } else {
            plan = planManager.createFromScratch(dto.getName());
        }
        return ResponseEntity.ok(toMapDeep(plan));
    }

    @PostMapping("/{id}/actions")
    public ResponseEntity<Map<String, Object>> addAction(@PathVariable Long id, @RequestBody ActionDTO dto) {
        ProposedAction action = new ProposedAction(dto.getName());
        action.setParty(dto.getParty());
        action.setLocation(dto.getLocation());
        if (dto.getDependsOn() != null) action.setDependsOn(dto.getDependsOn());
        Plan plan = planManager.addActionToPlan(id, action);
        return ResponseEntity.ok(toMapDeep(plan));
    }

    @PostMapping("/{id}/subplans")
    public ResponseEntity<Map<String, Object>> addSubPlan(@PathVariable Long id, @RequestBody PlanDTO dto) {
        Plan plan = planManager.addSubPlan(id, dto.getName());
        return ResponseEntity.ok(toMapDeep(plan));
    }

    @GetMapping("/{id}/report")
    public ResponseEntity<List<Map<String, Object>>> report(
            @PathVariable Long id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer depth) {
        return ResponseEntity.ok(planManager.generateReportData(id, status, depth));
    }

    @GetMapping("/{id}/metrics")
    public ResponseEntity<Map<String, Object>> metrics(@PathVariable Long id) {
        return ResponseEntity.ok(planManager.getMetrics(id));
    }

    private Map<String, Object> toMapShallow(Plan plan) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", plan.getId());
        map.put("name", plan.getName());
        map.put("status", plan.getStatus());
        map.put("actionCount", plan.getActions().size());
        map.put("subPlanCount", plan.getSubPlans().size());
        return map;
    }

    private Map<String, Object> toMapDeep(Plan plan) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", plan.getId());
        map.put("name", plan.getName());
        map.put("status", plan.getStatus());
        if (plan.getSourceProtocol() != null) {
            map.put("sourceProtocolId", plan.getSourceProtocol().getId());
            map.put("sourceProtocolName", plan.getSourceProtocol().getName());
        }
        map.put("targetStartDate", plan.getTargetStartDate());
        List<Map<String, Object>> children = new ArrayList<>();
        for (PlanNode child : plan.getChildren()) children.add(nodeToMap(child));
        map.put("children", children);
        return map;
    }

    private Map<String, Object> nodeToMap(PlanNode node) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", node.getId());
        map.put("name", node.getName());
        map.put("type", node.isLeaf() ? "ACTION" : "PLAN");
        map.put("status", node.getStatus());
        if (node instanceof Plan subPlan) {
            List<Map<String, Object>> children = new ArrayList<>();
            for (PlanNode child : subPlan.getChildren()) children.add(nodeToMap(child));
            map.put("children", children);
        }
        if (node instanceof ProposedAction action) {
            map.put("party", action.getParty());
            map.put("location", action.getLocation());
            map.put("dependsOn", action.getDependsOn());
            List<Map<String, Object>> allocations = new ArrayList<>();
            if (action.getAllocations() != null) {
                for (var alloc : action.getAllocations()) {
                    Map<String, Object> allocMap = new LinkedHashMap<>();
                    allocMap.put("id", alloc.getId());
                    allocMap.put("resourceTypeName", alloc.getResourceType().getName());
                    allocMap.put("quantity", alloc.getQuantity());
                    allocMap.put("kind", alloc.getKind().name());
                    allocMap.put("assetId", alloc.getAssetId());
                    allocations.add(allocMap);
                }
            }
            map.put("allocations", allocations);
        }
        return map;
    }
}