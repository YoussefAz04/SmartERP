package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.dto.SavTicketRequest;
import BuissnesManager.BuissnesManager.dto.SavInterventionRequest;
import BuissnesManager.BuissnesManager.entity.SavApprovalStatus;
import BuissnesManager.BuissnesManager.entity.SavStatus;
import BuissnesManager.BuissnesManager.service.SavTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sav")
@CrossOrigin(origins = "http://localhost:4200")
public class SavTicketController {

    @Autowired
    private SavTicketService savTicketService;

    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @GetMapping
    public ResponseEntity<?> getAllTickets() {
        try {
            return ResponseEntity.ok(savTicketService.getAllTickets());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getTicket(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(savTicketService.getTicket(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        try {
            return ResponseEntity.ok(savTicketService.getStats());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @GetMapping("/agents")
    public ResponseEntity<?> getAgents() {
        return ResponseEntity.ok(savTicketService.getAgents());
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @GetMapping("/eligible-sales")
    public ResponseEntity<?> getEligibleSales(@RequestParam Long clientId) {
        try {
            return ResponseEntity.ok(savTicketService.getEligibleSales(clientId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @GetMapping("/{id}/history")
    public ResponseEntity<?> getHistory(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(savTicketService.getHistory(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @PostMapping("/{id}/notes")
    public ResponseEntity<?> addNote(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(savTicketService.addNote(id, body.get("message")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @GetMapping("/{id}/interventions")
    public ResponseEntity<?> getInterventions(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(savTicketService.getInterventions(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @PostMapping("/{id}/interventions")
    public ResponseEntity<?> addIntervention(
            @PathVariable Long id,
            @RequestBody SavInterventionRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(savTicketService.addIntervention(id, request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/interventions/{interventionId}")
    public ResponseEntity<?> deleteIntervention(
            @PathVariable Long id,
            @PathVariable Long interventionId) {
        try {
            savTicketService.deleteIntervention(id, interventionId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @PatchMapping("/{id}/approval")
    public ResponseEntity<?> updateApproval(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(savTicketService.updateApproval(
                    id, SavApprovalStatus.valueOf(body.getOrDefault("approvalStatus", ""))));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @PostMapping
    public ResponseEntity<?> createTicket(@RequestBody SavTicketRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(savTicketService.createTicket(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTicket(@PathVariable Long id, @RequestBody SavTicketRequest request) {
        try {
            return ResponseEntity.ok(savTicketService.updateTicket(id, request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String rawStatus = body.get("status");
        if (rawStatus == null || rawStatus.isBlank()) {
            return ResponseEntity.badRequest().body("Status is required");
        }
        final SavStatus status;
        try {
            status = SavStatus.valueOf(rawStatus);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid SAV status");
        }
        try {
            return ResponseEntity.ok(savTicketService.updateStatus(id, status));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTicket(@PathVariable Long id) {
        try {
            savTicketService.deleteTicket(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
