package BuissnesManager.BuissnesManager.dto;

import java.util.List;

public final class SidebarModuleDtos {
    private SidebarModuleDtos() {
    }

    public record ModuleTitleResponse(
            Long id,
            String moduleTittle,
            List<ModuleResponse> modules) {
    }

    public record ModuleResponse(
            Long id,
            String title,
            boolean status,
            List<SubModuleResponse> sousModules) {
    }

    public record SubModuleResponse(
            Long id,
            String titreSousModule,
            String link) {
    }
}
