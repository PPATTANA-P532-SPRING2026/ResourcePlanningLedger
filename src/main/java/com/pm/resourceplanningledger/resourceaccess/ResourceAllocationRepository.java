package com.pm.resourceplanningledger.resourceaccess;

import com.pm.resourceplanningledger.domain.operational.ResourceAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceAllocationRepository extends JpaRepository<ResourceAllocation, Long> {
}