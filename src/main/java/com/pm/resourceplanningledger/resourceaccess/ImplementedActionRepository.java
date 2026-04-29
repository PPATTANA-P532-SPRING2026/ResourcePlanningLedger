package com.pm.resourceplanningledger.resourceaccess;

import com.pm.resourceplanningledger.domain.operational.ImplementedAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImplementedActionRepository extends JpaRepository<ImplementedAction, Long> {
    Optional<ImplementedAction> findByProposedActionId(Long proposedActionId);
}