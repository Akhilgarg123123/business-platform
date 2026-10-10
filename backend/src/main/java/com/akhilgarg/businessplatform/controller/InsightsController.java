package com.akhilgarg.businessplatform.controller;

import com.akhilgarg.businessplatform.dto.CustomerInsightsResponse;
import com.akhilgarg.businessplatform.dto.OverviewInsightsResponse;
import com.akhilgarg.businessplatform.security.CurrentUser;
import com.akhilgarg.businessplatform.service.InsightsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class InsightsController {

    private final InsightsService insightsService;
    private final CurrentUser currentUser;

    public InsightsController(InsightsService insightsService, CurrentUser currentUser) {
        this.insightsService = insightsService;
        this.currentUser = currentUser;
    }

    @GetMapping("/insights/overview")
    public ResponseEntity<OverviewInsightsResponse> getOverview() {
        return ResponseEntity.ok(insightsService.getOverview(currentUser.getId()));
    }

    @GetMapping("/customers/{id}/insights")
    public ResponseEntity<CustomerInsightsResponse> getCustomerInsights(@PathVariable Long id) {
        return ResponseEntity.ok(insightsService.getCustomerInsights(id, currentUser.getId()));
    }
}