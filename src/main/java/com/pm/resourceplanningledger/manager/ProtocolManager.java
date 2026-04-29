package com.pm.resourceplanningledger.manager;

import com.pm.resourceplanningledger.domain.knowledge.Protocol;
import com.pm.resourceplanningledger.resourceaccess.ProtocolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProtocolManager {

    private final ProtocolRepository protocolRepository;

    public ProtocolManager(ProtocolRepository protocolRepository) {
        this.protocolRepository = protocolRepository;
    }

    public List<Protocol> findAll() {
        return protocolRepository.findAll();
    }

    public Protocol findById(Long id) {
        return protocolRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Protocol not found: " + id));
    }

    @Transactional
    public Protocol create(Protocol protocol) {
        if (protocol.getSteps() != null) {
            protocol.getSteps().forEach(step -> step.setProtocol(protocol));
        }
        return protocolRepository.save(protocol);
    }

    @Transactional
    public Protocol update(Long id, Protocol updated) {
        Protocol existing = findById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.getSteps().clear();
        if (updated.getSteps() != null) {
            updated.getSteps().forEach(step -> {
                step.setProtocol(existing);
                existing.getSteps().add(step);
            });
        }
        return protocolRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        protocolRepository.deleteById(id);
    }
}