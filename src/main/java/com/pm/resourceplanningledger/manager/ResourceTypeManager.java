package com.pm.resourceplanningledger.manager;

import com.pm.resourceplanningledger.domain.knowledge.ResourceType;
import com.pm.resourceplanningledger.domain.ledger.Account;
import com.pm.resourceplanningledger.resourceaccess.AccountRepository;
import com.pm.resourceplanningledger.resourceaccess.ResourceTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ResourceTypeManager {

    private final ResourceTypeRepository resourceTypeRepository;
    private final AccountRepository accountRepository;

    public ResourceTypeManager(ResourceTypeRepository resourceTypeRepository,
                               AccountRepository accountRepository) {
        this.resourceTypeRepository = resourceTypeRepository;
        this.accountRepository = accountRepository;
    }

    public List<ResourceType> findAll() {
        return resourceTypeRepository.findAll();
    }

    public ResourceType findById(Long id) {
        return resourceTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ResourceType not found: " + id));
    }

    @Transactional
    public ResourceType create(ResourceType resourceType) {
        // Auto-create pool account
        Account poolAccount = new Account(
                "Pool - " + resourceType.getName(),
                Account.AccountKind.POOL,
                resourceType
        );
        poolAccount = accountRepository.save(poolAccount);
        resourceType.setPoolAccount(poolAccount);

        // Auto-create alert memo account and posting rule
        Account alertAccount = new Account(
                "Alert - " + resourceType.getName(),
                Account.AccountKind.ALERT_MEMO,
                resourceType
        );
        accountRepository.save(alertAccount);

        return resourceTypeRepository.save(resourceType);
    }

    @Transactional
    public ResourceType update(Long id, ResourceType updated) {
        ResourceType existing = findById(id);
        existing.setName(updated.getName());
        existing.setKind(updated.getKind());
        existing.setUnit(updated.getUnit());
        return resourceTypeRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        resourceTypeRepository.deleteById(id);
    }
}