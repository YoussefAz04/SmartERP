package BuissnesManager.BuissnesManager.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionDto {
        private Long id;
        private boolean create;
        private boolean delete;
        private boolean update;
        private boolean view;

        private Long moduleId;
        private String moduleTitle;

        private Long roleId;
    }

