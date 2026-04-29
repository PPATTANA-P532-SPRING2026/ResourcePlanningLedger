package com.pm.resourceplanningledger.manager;

import com.pm.resourceplanningledger.domain.knowledge.Protocol;
import com.pm.resourceplanningledger.domain.knowledge.ProtocolStep;
import com.pm.resourceplanningledger.domain.operational.*;
import com.pm.resourceplanningledger.resourceaccess.PlanRepository;
import com.pm.resourceplanningledger.resourceaccess.ProtocolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PlanManager {

    private final PlanRepository planRepository;
    private final ProtocolRepository protocolRepository;

    public PlanManager(PlanRepository planRepository, ProtocolRepository protocolRepository) {
        this.planRepository = planRepository;
        this.protocolRepository = protocolRepository;
    }

    public List<Plan> findAllTopLevel() {
        return planRepository.findByParentPlanIsNull();
    }

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

        // Create one ProposedAction per protocol step
        for (ProtocolStep step : protocol.getSteps()) {
            ProposedAction action = new ProposedAction(step.getName());
            action.setProtocol(protocol);
            // Copy dependencies
            if (step.getDependsOn() != null) {
                action.setDependsOn(new ArrayList<>(step.getDependsOn()));
            }
            plan.addAction(action);
        }

        return planRepository.save(plan);
    }

    @Transactional
    public Plan createFromScratch(String planName) {
        Plan plan = new Plan(planName);
        return planRepository.save(plan);
    }

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

    public String generateReport(Long planId) {
        Plan plan = findById(planId);
        StringBuilder report = new StringBuilder();
        DepthFirstPlanIterator iterator = new DepthFirstPlanIterator(plan);

        while (iterator.hasNext()) {
            PlanNode node = iterator.next();
            String type = node.isLeaf() ? "Action" : "Plan";
            report.append(String.format("[%s] %s — Status: %s%n", type, node.getName(), node.getStatus()));
        }
        return report.toString();
    }

    public List<Map<String, Object>> generateReportData(Long planId) {
        Plan plan = findById(planId);
        List<Map<String, Object>> nodes = new ArrayList<>();
        DepthFirstPlanIterator iterator = new DepthFirstPlanIterator(plan);

        while (iterator.hasNext()) {
            PlanNode node = iterator.next();
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", node.getId());
            entry.put("name", node.getName());
            entry.put("type", node.isLeaf() ? "ACTION" : "PLAN");
            entry.put("status", node.getStatus());
            entry.put("isLeaf", node.isLeaf());
            nodes.add(entry);
        }
        return nodes;
    }
}