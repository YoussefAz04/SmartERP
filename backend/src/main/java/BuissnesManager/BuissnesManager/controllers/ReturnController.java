package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.dto.FinancialDtos.*;
import BuissnesManager.BuissnesManager.entity.ReturnType;
import BuissnesManager.BuissnesManager.service.ReturnService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/returns")
@PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
public class ReturnController {
    private final ReturnService returnService;

    public ReturnController(ReturnService returnService) {
        this.returnService = returnService;
    }

    @GetMapping
    public List<ReturnResponse> getReturns() {
        return returnService.getReturns();
    }

    @GetMapping("/eligible-lines")
    public List<EligibleReturnLine> eligibleLines(@RequestParam ReturnType type,
                                                  @RequestParam Long invoiceId) {
        return returnService.getEligibleLines(type, invoiceId);
    }

    @PostMapping
    public ResponseEntity<ReturnResponse> createDraft(@Valid @RequestBody ReturnRequest request) {
        return ResponseEntity.ok(returnService.createDraft(request));
    }

    @PostMapping("/{id}/validate")
    public ReturnResponse validate(@PathVariable Long id) {
        return returnService.validate(id);
    }

    @PostMapping("/{id}/cancel")
    public ReturnResponse cancel(@PathVariable Long id) {
        return returnService.cancelDraft(id);
    }
}
