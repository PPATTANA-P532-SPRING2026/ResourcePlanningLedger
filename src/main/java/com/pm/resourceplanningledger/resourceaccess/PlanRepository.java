package com.pm.resourceplanningledger.resourceaccess;

import com.pm.resourceplanningledger.domain.operational.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findByParentPlanIsNull();
}