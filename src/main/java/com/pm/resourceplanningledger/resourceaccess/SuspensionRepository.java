package com.pm.resourceplanningledger.resourceaccess;

import com.pm.resourceplanningledger.domain.operational.Suspension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuspensionRepository extends JpaRepository<Suspension, Long> {
}