package com.pm.resourceplanningledger.resourceaccess;

import com.pm.resourceplanningledger.domain.knowledge.Protocol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProtocolRepository extends JpaRepository<Protocol, Long> {
}