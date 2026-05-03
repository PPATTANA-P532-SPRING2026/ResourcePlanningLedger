package com.pm.resourceplanningledger.manager;

import com.pm.resourceplanningledger.domain.knowledge.ResourceType;
import com.pm.resourceplanningledger.domain.operational.*;
import com.pm.resourceplanningledger.resourceaccess.PlanRepository;
import com.pm.resourceplanningledger.resourceaccess.ProtocolRepository;
import com.pm.resourceplanningledger.resourceaccess.ResourceTypeRepository;
import com.pm.resourceplanningledger.domain.knowledge.Protocol;
import com.pm.resourceplanningledger.domain.knowledge.ProtocolStep;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class PlanManager {

    private final PlanRepository planRepository;
    private final ProtocolRepository protocolRepository;
    private final ResourceTypeRepository resourceTypeRepository;

    public PlanManager(PlanRepository planRepository, ProtocolRepository protocolRepository,
                       ResourceTypeRepository resourceTypeRepository) {
        this.planRepository = planRepository;
        this.protocolRepository = protocolRepository;
        this.resourceTypeRepository = resourceTypeRepository;
    }

    public List<Plan> findAllTopLevel() { return planRepository.findByParentPlanIsNull(); }

    public Plan findById(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + id));
    }

    @Transactional
    public Plan createFromProtocol(Long protocolId, String planName) {
        Protocol protocol = protocolRepository.findById(protocolId)
                .orElseThrow(() -> new IllegalArgumentException("Protocol not found: " + protocolId));
        Plan plan = new Plan(planName);
        plan.setSourceProtocol(protocol);
        for (ProtocolStep step : protocol.getSteps()) {
            ProposedAction action = new ProposedAction(step.getName());
            action.setProtocol(protocol);
            if (step.getDependsOn() != null) action.setDependsOn(new ArrayList<>(step.getDependsOn()));
            plan.addAction(action);
        }
        return planRepository.save(plan);
    }

    @Transactional
    public Plan createFromScratch(String planName) { return planRepository.save(new Plan(planName)); }

    @Transactional
    public Plan addActionToPlan(Long planId, ProposedAction action) {
        Plan plan = findById(planId);
        plan.addAction(action);
        return planRepository.save(plan);
    }

    @Transactional
    public Plan addSubPlan(Long parentPlanId, String subPlanName) {
        Plan parent = findById(parentPlanId);
        Plan subPlan = new Plan(subPlanName);
        parent.addSubPlan(subPlan);
        return planRepository.save(parent);
    }

    public List<Map<String, Object>> generateReportData(Long planId, String statusFilter, Integer depthLimit) {
        Plan plan = findById(planId);
        List<ResourceType> allResourceTypes = resourceTypeRepository.findAll();
        List<Map<String, Object>> nodes = new ArrayList<>();

        Iterator<PlanNode> iterator;
        if (depthLimit != null) {
            iterator = new LazySubtreeIterator(plan, depthLimit);
        } else {
            iterator = new DepthFirstPlanIterator(plan);
        }

        if (statusFilter != null && !statusFilter.isEmpty()) {
            String filter = statusFilter;
            iterator = new FilteredPlanIterator(plan, node -> filter.equals(node.getStatus()));
        }

        while (iterator.hasNext()) {
            PlanNode node = iterator.next();
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", node.getId());
            entry.put("name", node.getName());
            entry.put("type", node.isLeaf() ? "ACTION" : "PLAN");
            entry.put("status", node.getStatus());
            entry.put("isLeaf", node.isLeaf());

            Map<String, Object> allocations = new LinkedHashMap<>();
            for (ResourceType rt : allResourceTypes) {
                var qty = node.getTotalAllocatedQuantity(rt);
                if (qty.signum() > 0) {
                    allocations.put(rt.getName() + " (" + rt.getUnit() + ")", qty);
                }
            }
            entry.put("allocatedQuantities", allocations);
            nodes.add(entry);
        }
        return nodes;
    }

    public Map<String, Object> getMetrics(Long planId) {
        Plan plan = findById(planId);

        CompletionRatioVisitor completionVisitor = new CompletionRatioVisitor();
        plan.accept(completionVisitor);

        ResourceCostVisitor costVisitor = new ResourceCostVisitor();
        plan.accept(costVisitor);

        RiskScoreVisitor riskVisitor = new RiskScoreVisitor();
        plan.accept(riskVisitor);

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("completionRatio", completionVisitor.getRatio());
        metrics.put("completedLeaves", completionVisitor.getCompletedLeaves());
        metrics.put("totalLeaves", completionVisitor.getTotalLeaves());
        metrics.put("totalCost", costVisitor.getTotalCost());
        metrics.put("riskScore", riskVisitor.getScore());
        return metrics;
    }
}