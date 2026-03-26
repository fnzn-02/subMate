package com.onAir.submate.domain.subscription.controller;

import com.onAir.submate.domain.subscription.dto.SubscriptionRequest;
import com.onAir.submate.domain.subscription.dto.SubscriptionResponse;
import com.onAir.submate.domain.subscription.service.SubscriptionService;
import com.onAir.submate.global.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<SubscriptionResponse> create(@Valid @RequestBody SubscriptionRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subscriptionService.create(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<SubscriptionResponse>> getMySubscriptions() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(subscriptionService.getMySubscriptions(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(subscriptionService.update(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        subscriptionService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/reorder")
    public ResponseEntity<Void> reorder(@RequestBody Map<String, List<Long>> body) {
        Long userId = SecurityUtils.getCurrentUserId();
        subscriptionService.reorder(userId, body.get("ids"));
        return ResponseEntity.noContent().build();
    }
}
