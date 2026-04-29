package com.pm.resourceplanningledger.resourceaccess;

import com.pm.resourceplanningledger.domain.operational.ProposedAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProposedActionRepository extends JpaRepository<ProposedAction, Long> {
}