package com.pm.resourceplanningledger.client;

import com.pm.resourceplanningledger.domain.knowledge.ResourceType;
import com.pm.resourceplanningledger.dto.ResourceTypeDTO;
import com.pm.resourceplanningledger.manager.ResourceTypeManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resource-types")
public class ResourceTypeController {

    private final ResourceTypeManager resourceTypeManager;

    public ResourceTypeController(ResourceTypeManager resourceTypeManager) {
        this.resourceTypeManager = resourceTypeManager;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list() {
        List<ResourceType> types = resourceTypeManager.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (ResourceType rt : types) {
            result.add(toMap(rt));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable Long id) {
        return ResponseEntity.ok(toMap(resourceTypeManager.findById(id)));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody ResourceTypeDTO dto) {
        ResourceType rt = new ResourceType(
                dto.getName(),
                ResourceType.ResourceKind.valueOf(dto.getKind()),
                dto.getUnit()
        );
        double initialBalance = dto.getInitialBalance() != null ? dto.getInitialBalance() : 0.0;
        ResourceType saved = resourceTypeManager.create(rt, initialBalance);
        return ResponseEntity.ok(toMap(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody ResourceTypeDTO dto) {
        ResourceType rt = new ResourceType(
                dto.getName(),
                ResourceType.ResourceKind.valueOf(dto.getKind()),
                dto.getUnit()
        );
        ResourceType updated = resourceTypeManager.update(id, rt);
        return ResponseEntity.ok(toMap(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        resourceTypeManager.delete(id);
        return ResponseEntity.noContent().build();
    }

    private Map<String, Object> toMap(ResourceType rt) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", rt.getId());
        map.put("name", rt.getName());
        map.put("kind", rt.getKind().name());
        map.put("unit", rt.getUnit());
        if (rt.getPoolAccount() != null) {
            map.put("poolAccountId", rt.getPoolAccount().getId());
        }
        return map;
    }
}