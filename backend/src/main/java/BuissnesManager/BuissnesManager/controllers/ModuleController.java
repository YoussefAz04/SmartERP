package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.entity.Module;
import BuissnesManager.BuissnesManager.entity.ModuleTittle;
import BuissnesManager.BuissnesManager.entity.SousModule;
import BuissnesManager.BuissnesManager.service.ModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("module")
public class ModuleController {

    @Autowired
    private ModuleService moduleService ;

    @GetMapping("getAllModulesTittle")
    public ResponseEntity<?> getAllModulesTittle() {
        List<ModuleTittle> modules = moduleService.getAllModuleTitlle();
        return ResponseEntity.ok(modules);
    }

    @GetMapping("getAllModule")
    public ResponseEntity<?> getAllModules() {
        List<Module> modules = moduleService.getAllModule();
        return ResponseEntity.ok(modules);
    }
    @GetMapping("getAllSousModule")
        public ResponseEntity<?> getAllSousModule() {
        List<SousModule> sousModules = moduleService.getAllSousModule();
        return ResponseEntity.ok(sousModules);
    }

    @GetMapping("getModule/{id}")
    public ResponseEntity<Module> getModuleById(@PathVariable Long id) {
        try {
            Module module = moduleService.getByIdModule(id);
            return ResponseEntity.ok(module);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("getModuleByRole/{idRole}")
    public ResponseEntity<?> getModuleByRole(@PathVariable Long idRole) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(moduleService.getModuleTittleByRole(idRole));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("getModuleTittleByRole/{idRole}")
    public ResponseEntity<?> getModuleTittleByRole(@PathVariable Long idRole) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(moduleService.getAllModuleTittleByIdRole(idRole));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/update-status/{roleId}")
    public ResponseEntity<String> updateStatusByRole(@PathVariable("roleId") Long roleId) {
        try {
            moduleService.updateStatusModuleByRole(roleId);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la mise à jour du statut des modules.");
        }
    }
    @PostMapping("/addModuleTittle")
    public ResponseEntity<?> addModuleTittle (@RequestBody ModuleTittle moduleTittle){
        try {
            return ResponseEntity.status(HttpStatus.OK).body(moduleService.addModuleTittle(moduleTittle));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/addModule")
    public ResponseEntity<?> addModule (@RequestBody Module module){
        try {
            return ResponseEntity.status(HttpStatus.OK).body(moduleService.addModule(module));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("deleteModuleTittle/{id}")
    public ResponseEntity<?>  deleteModuleTittle(@PathVariable Long idModuleTittle){
        moduleService.deleteModuleTittle(idModuleTittle);
        return ResponseEntity.ok().build();
    }


    @GetMapping("getAllModuleNotAffecter/{id}")
    public ResponseEntity<?> getAllModuleNotAffecter(@PathVariable Long id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(moduleService.getAllModuleNotAffecter(id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    @PostMapping("affecterModuleTittle/{idModuleTittle}")
    public ResponseEntity<?> affecterModuleToModuleTittle(@RequestBody List<Module> modules , @PathVariable Long idModuleTittle){
        this.moduleService.affecterModuleToModuleTittle(modules,idModuleTittle);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/addModuleWithTitreModule/{idModuleTitre}")
    public ResponseEntity<?> addModuleWithTitreModule (@RequestBody Module module,@PathVariable Long idModuleTitre){
        try {
            return ResponseEntity.status(HttpStatus.OK).body(moduleService.addModuleWithTitreModule(module,idModuleTitre));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    @PostMapping("/createSousModule/{idModule}")
    public ResponseEntity<?> createSousModule (@RequestBody SousModule sousModule,@PathVariable Long idModule){
        try {
            return ResponseEntity.status(HttpStatus.OK).body(moduleService.createSousModule(sousModule,idModule));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }







}
