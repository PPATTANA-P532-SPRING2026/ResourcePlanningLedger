package com.pm.resourceplanningledger.client;

import com.pm.resourceplanningledger.domain.knowledge.Protocol;
import com.pm.resourceplanningledger.domain.knowledge.ProtocolStep;
import com.pm.resourceplanningledger.dto.ProtocolDTO;
import com.pm.resourceplanningledger.manager.ProtocolManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/protocols")
public class ProtocolController {

    private final ProtocolManager protocolManager;

    public ProtocolController(ProtocolManager protocolManager) {
        this.protocolManager = protocolManager;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list() {
        List<Protocol> protocols = protocolManager.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Protocol p : protocols) {
            result.add(toMap(p));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable Long id) {
        return ResponseEntity.ok(toMap(protocolManager.findById(id)));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody ProtocolDTO dto) {
        Protocol protocol = new Protocol(dto.getName(), dto.getDescription());
        if (dto.getSteps() != null) {
            for (ProtocolDTO.ProtocolStepDTO stepDto : dto.getSteps()) {
                ProtocolStep step = new ProtocolStep(stepDto.getName());
                if (stepDto.getDependsOn() != null) {
                    step.setDependsOn(stepDto.getDependsOn());
                }
                protocol.addStep(step);
            }
        }
        Protocol saved = protocolManager.create(protocol);
        return ResponseEntity.ok(toMap(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody ProtocolDTO dto) {
        Protocol protocol = new Protocol(dto.getName(), dto.getDescription());
        if (dto.getSteps() != null) {
            for (ProtocolDTO.ProtocolStepDTO stepDto : dto.getSteps()) {
                ProtocolStep step = new ProtocolStep(stepDto.getName());
                if (stepDto.getDependsOn() != null) {
                    step.setDependsOn(stepDto.getDependsOn());
                }
                protocol.addStep(step);
            }
        }
        Protocol updated = protocolManager.update(id, protocol);
        return ResponseEntity.ok(toMap(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        protocolManager.delete(id);
        return ResponseEntity.noContent().build();
    }

    private Map<String, Object> toMap(Protocol p) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", p.getId());
        map.put("name", p.getName());
        map.put("description", p.getDescription());
        List<Map<String, Object>> steps = new ArrayList<>();
        if (p.getSteps() != null) {
            for (ProtocolStep s : p.getSteps()) {
                Map<String, Object> stepMap = new LinkedHashMap<>();
                stepMap.put("id", s.getId());
                stepMap.put("name", s.getName());
                stepMap.put("dependsOn", s.getDependsOn());
                steps.add(stepMap);
            }
        }
        map.put("steps", steps);
        return map;
    }
}