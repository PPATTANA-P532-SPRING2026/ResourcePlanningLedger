package com.pm.resourceplanningledger.resourceaccess;

import com.pm.resourceplanningledger.domain.ledger.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByKind(Account.AccountKind kind);
    Account findByName(String name);
}