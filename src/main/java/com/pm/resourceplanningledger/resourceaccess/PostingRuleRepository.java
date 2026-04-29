package com.pm.resourceplanningledger.resourceaccess;

import com.pm.resourceplanningledger.domain.ledger.Account;
import com.pm.resourceplanningledger.domain.ledger.PostingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostingRuleRepository extends JpaRepository<PostingRule, Long> {
    List<PostingRule> findByTriggerAccount(Account triggerAccount);
}