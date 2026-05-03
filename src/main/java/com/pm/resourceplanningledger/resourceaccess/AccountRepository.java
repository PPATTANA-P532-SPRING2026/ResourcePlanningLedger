package com.pm.resourceplanningledger.resourceaccess;

import com.pm.resourceplanningledger.domain.ledger.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByKind(Account.AccountKind kind);
    Optional<Account> findFirstByName(String name);
}