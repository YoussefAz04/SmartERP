package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.Module;
import BuissnesManager.BuissnesManager.entity.ModuleTittle;
import BuissnesManager.BuissnesManager.entity.RoleType;
import BuissnesManager.BuissnesManager.entity.SousModule;
import BuissnesManager.BuissnesManager.repository.ModuleRepo;
import BuissnesManager.BuissnesManager.repository.ModuleTittleRepo;
import BuissnesManager.BuissnesManager.repository.PermissionRepo;
import BuissnesManager.BuissnesManager.repository.RoleRepo;
import BuissnesManager.BuissnesManager.repository.SousModuleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ModuleService {

    
    private static final Set<String> ADMIN_ONLY_KEYWORDS = Set.of(
            "user", "utilisateur", "permission", "role", "parametre", "paramètre", "setting"
    );

    @Autowired
    private ModuleRepo moduleRepo;
    @Autowired
    private PermissionRepo permissionRepo;

    @Autowired
    private ModuleTittleRepo moduleTittleRepo;

    @Autowired
    private SousModuleRepo sousModuleRepo ;

    @Autowired
    private RoleRepo roleRepo ;

    public List<ModuleTittle> getAllModuleTitlle() {
        return moduleTittleRepo.findAll();
    }

    public List<Module> getAllModule() {
        return moduleRepo.findAll();
    }
    public List<SousModule> getAllSousModule(){
        return sousModuleRepo.findAll();
    }

    public Module getByIdModule(Long idModule) {
        return moduleRepo.findById(idModule)
                .orElseThrow(() -> new RuntimeException("Module non trouvé avec l'id : " + idModule));
    }


    public List<Module> getModuleTittleByRole(Long idRole) {
        return permissionRepo.getModuleByIdRole(idRole);
    }

    public Set<ModuleTittle> getAllModuleTittleByIdRole(Long idRole) {
        List<Module> modules = permissionRepo.getModuleByIdRole(idRole);
        Set<ModuleTittle> moduleTittles = new HashSet<>();

        for (Module module : modules) {
            moduleTittles.add(moduleTittleRepo.findByModules(module));
        }
        return moduleTittles;
    }

    public void updateStatusModuleByRole(Long idRoleUser) {
        List<Module> allModules = moduleRepo.findAll();
        List<Module> modulesAffecteByRole = permissionRepo.getModuleByIdRole(idRoleUser);

        Set<Long> idsAffectes = modulesAffecteByRole.stream()
                .map(Module::getId)
                .collect(Collectors.toSet());

        
        
        
        roleRepo.findById(idRoleUser).ifPresent(role -> {
            if (RoleType.EMPLOYEE.equals(role.getRoleType())) {
                for (Module module : allModules) {
                    ModuleTittle mt = module.getModuleTittle();
                    if (mt != null) {
                        String title = mt.getModuleTittle() != null
                                ? mt.getModuleTittle().toLowerCase()
                                : "";
                        
                        boolean isAdminOnly = ADMIN_ONLY_KEYWORDS.stream()
                                .anyMatch(title::contains);
                        if (!isAdminOnly) {
                            idsAffectes.add(module.getId());
                        }
                    } else {
                        
                        idsAffectes.add(module.getId());
                    }
                }
            }
        });

        for (Module module : allModules) {
            if (idsAffectes.contains(module.getId())) {
                module.setStatus(true);
            } else {
                module.setStatus(false);
            }
            moduleRepo.save(module);
        }
    }

    public ModuleTittle addModuleTittle(ModuleTittle moduleTittle) {
        return moduleTittleRepo.save(moduleTittle);
    }

    public Module addModule(Module module) {
        return moduleRepo.save(module);
    }


    public void deleteModuleTittle(Long id) {
        moduleTittleRepo.deleteById(id);
    }

    public List<Module> getAllModuleNotAffecter(Long idModuleTittle) {
        ModuleTittle moduleTittle = moduleTittleRepo.findById(idModuleTittle).orElse(null);
        if (moduleTittle == null) {
            return Collections.emptyList();
        }
            List<Module> modules = moduleRepo.findAll();
            return modules.stream()
                    .filter(module -> !Objects.equals(
                            module.getModuleTittle() != null ? module.getModuleTittle().getId() : null,
                            moduleTittle.getId()))
                    .collect(Collectors.toList());
        }


        public void affecterModuleToModuleTittle(List<Module> modules , Long idModuleTittle){
           ModuleTittle moduleTittle = moduleTittleRepo.findById(idModuleTittle)
                   .orElseThrow(() -> new RuntimeException("ModuleTittle non trouvé avec l'id : " + idModuleTittle));
           for (Module module: modules){
               module.setModuleTittle(moduleTittle);
               moduleRepo.save(module);
           }
        }


        public Module addModuleWithTitreModule(Module module,Long idModuleTitre){
           ModuleTittle  moduleTittle = moduleTittleRepo.findById(idModuleTitre)
                   .orElseThrow(() -> new RuntimeException("ModuleTittle non trouvé avec l'id : " + idModuleTitre));
           module.setModuleTittle(moduleTittle);
           return moduleRepo.save(module);
        }
        public SousModule createSousModule(SousModule sousModule,Long idModule){
            Module module = moduleRepo.findById(idModule)
                    .orElseThrow(() -> new RuntimeException("Module non trouvé avec l'id : " + idModule));
            sousModule.setModule(module);
            return sousModuleRepo.save(sousModule);
        }





}

