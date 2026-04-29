package com.pm.resourceplanningledger.resourceaccess;

import com.pm.resourceplanningledger.domain.ledger.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}