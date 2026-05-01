package BuissnesManager.BuissnesManager.dto;

/**
 * Per-module CRUD permission request used when creating an employee.
 * Allows callers to grant only a subset of the four CRUD actions on a module
 * (e.g. read-only access on one module and full access on another).
 */
public class ModulePermissionRequest {
    private Long moduleId;
    private boolean create;
    private boolean update;
    private boolean view;
    private boolean delete;

    public Long getModuleId() { return moduleId; }
    public void setModuleId(Long moduleId) { this.moduleId = moduleId; }

    public boolean isCreate() { return create; }
    public void setCreate(boolean create) { this.create = create; }

    public boolean isUpdate() { return update; }
    public void setUpdate(boolean update) { this.update = update; }

    public boolean isView() { return view; }
    public void setView(boolean view) { this.view = view; }

    public boolean isDelete() { return delete; }
    public void setDelete(boolean delete) { this.delete = delete; }

    /** True when at least one of the four CRUD flags is set. */
    public boolean hasAnyFlag() {
        return create || update || view || delete;
    }
}
