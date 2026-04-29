package com.pm.resourceplanningledger.resourceaccess;

import com.pm.resourceplanningledger.domain.knowledge.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceTypeRepository extends JpaRepository<ResourceType, Long> {
}