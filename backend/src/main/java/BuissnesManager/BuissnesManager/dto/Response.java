package BuissnesManager.BuissnesManager.dto;

import BuissnesManager.BuissnesManager.entity.Entreprise;
import BuissnesManager.BuissnesManager.entity.Role;
import lombok.Data;

import java.util.List;
@Data
public class Response {


    private Long  idUsers ;
    private Long idRoleUser;
    private String userName ;
    private List<PermissionDto> permissionDtos ;
    private String token ;
    private Entreprise entreprise ;


}
