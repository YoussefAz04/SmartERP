package BuissnesManager.BuissnesManager.controllers;


import BuissnesManager.BuissnesManager.entity.Departement;
import BuissnesManager.BuissnesManager.service.DepartementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("departement")
@RestController
public class DepartementController {
    @Autowired
    private DepartementService departementService ;


    @PostMapping("/add/{entrepriseId}")
    public ResponseEntity<Departement> createDepartement(@RequestBody Departement d, @PathVariable Long entrepriseId) {
        return ResponseEntity.ok(departementService.createDepartement(d, entrepriseId));
    }
    @GetMapping("/all")
    public ResponseEntity<?> getAllDepartements() {
        return ResponseEntity.ok(departementService.getAllDepartements());
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getDepartementDTo(@PathVariable Long id) {
        return ResponseEntity.ok(departementService.getDepartementDTOById(id));
    }
    @GetMapping("getbyID/{id}")
    public ResponseEntity<?> getDepartement(@PathVariable Long id) {
        return ResponseEntity.ok(departementService.getDepartementById(id));
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<Departement> updateDepartement(@PathVariable Long id, @RequestBody Departement d) {
        return ResponseEntity.ok(departementService.updateDepartement(id, d));
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteDepartement(@PathVariable Long id) {
        departementService.deleteDepartement(id);
        return ResponseEntity.ok("Département supprimé avec succès.");
    }



}
