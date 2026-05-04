package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.CreateEmployeeRequest;
import BuissnesManager.BuissnesManager.dto.UserRequest;
import BuissnesManager.BuissnesManager.entity.Entreprise;
import BuissnesManager.BuissnesManager.entity.Module;
import BuissnesManager.BuissnesManager.entity.Permission;
import BuissnesManager.BuissnesManager.entity.Role;
import BuissnesManager.BuissnesManager.entity.User;
import BuissnesManager.BuissnesManager.dto.UserDTO;
import BuissnesManager.BuissnesManager.repository.EntrepriseRepo;
import BuissnesManager.BuissnesManager.repository.ModuleRepo;
import BuissnesManager.BuissnesManager.repository.PermissionRepo;
import BuissnesManager.BuissnesManager.repository.RoleRepo;
import BuissnesManager.BuissnesManager.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepo userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EntrepriseRepo entrepriseRepo ;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private PermissionRepo permissionRepo;

    @Autowired
    private ModuleRepo moduleRepo;

    public UserDTO createEmployeeWithModules(CreateEmployeeRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom d'utilisateur est requis");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("L'email est requis");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 6 caractères");
        }
        if (request.getEnterpriseId() == null) {
            throw new IllegalArgumentException("L'identifiant de l'entreprise est requis");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Un utilisateur avec ce nom existe déjà.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà.");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(request.getPassword());
        user.setEnabled(true);

        Entreprise entreprise = entrepriseRepo.findById(request.getEnterpriseId())
                .orElseThrow(() -> new IllegalArgumentException("Entreprise non trouvée avec l'ID: " + request.getEnterpriseId()));
        user.setEntreprise(entreprise);

        Role employeeRole = roleRepo.findByName("EMPLOYEE")
                .orElseThrow(() -> new RuntimeException("Le rôle EMPLOYEE n'existe pas"));
        user.setRoles(new ArrayList<>(List.of(employeeRole)));

        User savedUser = userRepository.save(user);

        if (request.getModulePermissions() != null && !request.getModulePermissions().isEmpty()) {
            for (BuissnesManager.BuissnesManager.dto.ModulePermissionRequest mp : request.getModulePermissions()) {
                if (mp == null || mp.getModuleId() == null || !mp.hasAnyFlag()) {
                    continue;
                }
                Long moduleId = mp.getModuleId();
                Module module = moduleRepo.findById(moduleId)
                        .orElseThrow(() -> new RuntimeException("Module non trouvé: " + moduleId));
                Permission existing = permissionRepo.findByModuleIdAndUserId(moduleId, savedUser.getId());
                if (existing != null) {
                    existing.setCreate(mp.isCreate());
                    existing.setUpdate(mp.isUpdate());
                    existing.setDelete(mp.isDelete());
                    existing.setView(mp.isView());
                    permissionRepo.save(existing);
                } else {
                    Permission permission = new Permission();
                    permission.setCreate(mp.isCreate());
                    permission.setUpdate(mp.isUpdate());
                    permission.setDelete(mp.isDelete());
                    permission.setView(mp.isView());
                    permission.setModule(module);
                    permission.setUser(savedUser);
                    permissionRepo.save(permission);
                }
            }
        } else if (request.getModuleIds() != null && !request.getModuleIds().isEmpty()) {
            for (Long moduleId : request.getModuleIds()) {
                Module module = moduleRepo.findById(moduleId)
                        .orElseThrow(() -> new RuntimeException("Module non trouvé: " + moduleId));
                if (permissionRepo.findByModuleIdAndUserId(moduleId, savedUser.getId()) == null) {
                    Permission permission = new Permission();
                    permission.setCreate(true);
                    permission.setUpdate(true);
                    permission.setDelete(true);
                    permission.setView(true);
                    permission.setModule(module);
                    permission.setUser(savedUser);
                    permissionRepo.save(permission);
                }
            }
        }

        return toUserDTO(savedUser);
    }

    public User createEmployee(String username, String email, String password, String phone, Long enterpriseId) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Un utilisateur avec ce nom existe déjà.");
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(password);
        user.setEnabled(true);
        user.setEntreprise(entrepriseRepo.findById(enterpriseId)
                .orElseThrow(() -> new RuntimeException("Enterprise not found")));
        Role employeeRole = roleRepo.findByName("EMPLOYEE")
                .orElseThrow(() -> new RuntimeException("EMPLOYEE role not found"));
        user.setRoles(new ArrayList<>(List.of(employeeRole)));
        return userRepository.save(user);
    }

    public User createUser(UserRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Un utilisateur avec ce nom existe déjà.");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setEnabled(true);

        return userRepository.save(user);
    }
    public User createUserWithEntreprise(UserRequest request, Long idEntreprise) {
        if (request == null || request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom d'utilisateur est requis");
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Un utilisateur avec ce nom existe déjà.");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(request.getPassword());
        user.setEnabled(true);

        Entreprise entreprise = entrepriseRepo.findById(idEntreprise)
                .orElseThrow(() -> new IllegalArgumentException("Entreprise non trouvée avec l'ID: " + idEntreprise));
        user.setEntreprise(entreprise);

        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }


    public void assignRoleToUser(Long userId, Long idRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Role role = roleRepo.findById(idRole)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.getRoles().add(role);
        userRepository.save(user);
    }

    public void removeRoleFromUser(Long userId, Long idRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Role role = roleRepo.findById(idRole)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.getRoles().remove(role);
        userRepository.save(user);
    }
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    // Get users belonging to a specific enterprise
    public List<User> getUsersByEnterprise(Long enterpriseId) {
        return userRepository.findByEntrepriseId(enterpriseId);
    }

    // Check if user belongs to the given enterprise
    public boolean isUserInEnterprise(Long userId, Long enterpriseId) {
        Optional<User> user = userRepository.findById(userId);
        return user.isPresent() && user.get().getEntreprise() != null
                && user.get().getEntreprise().getId().equals(enterpriseId);
    }

    // Validate enterprise access - throws if user does not belong to the enterprise
    public void validateEnterpriseAccess(Long userId, Long requiredEnterpriseId) {
        if (!isUserInEnterprise(userId, requiredEnterpriseId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "User does not belong to enterprise " + requiredEnterpriseId);
        }
    }

    public List<UserDTO> getAllUsersDTO() {
        List<User> users = userRepository.findAll();
        return toUserDTOList(users);
    }

    public List<UserDTO> getAllUsersDTOByEnterprise(Long enterpriseId) {
        List<User> users = userRepository.findByEntrepriseId(enterpriseId);
        return toUserDTOList(users);
    }

    private List<UserDTO> toUserDTOList(List<User> users) {
        return users.stream().map(this::toUserDTO).collect(Collectors.toList());
    }

    private UserDTO toUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setEnabled(user.isEnabled());
        dto.setPhone(user.getPhone());

        if (user.getRoles() != null) {
            dto.setRoles(user.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.toList()));
        }

        if (user.getEntreprise() != null) {
            dto.setEntrepriseId(user.getEntreprise().getId());
            dto.setEntrepriseNom(user.getEntreprise().getNom());
        }

        return dto;
    }


    public UserDTO getUserById(Long idUser){
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id : " + idUser));
        UserDTO dto =new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setEnabled(user.isEnabled());
        dto.setPhone(user.getPhone());
        if (user.getRoles() != null) {
            dto.setRoles(user.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.toList()));
        }
        if (user.getEntreprise() != null) {
            dto.setEntrepriseId(user.getEntreprise().getId());
            dto.setEntrepriseNom(user.getEntreprise().getNom());
        }
        return  dto ;


    }
    public void updateUser(UserDTO userDTO , Long idUser){
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id : " + idUser));
        user.setUsername(userDTO.getUsername());
        user.setPhone(userDTO.getPhone());
        Entreprise entreprise = entrepriseRepo.findById(userDTO.getEntrepriseId())
                .orElseThrow(() -> new RuntimeException("Entreprise non trouvée"));
        user.setEntreprise(entreprise);
        user.setEnabled(userDTO.isEnabled());

        if (userDTO.getRoles() != null && !userDTO.getRoles().isEmpty()) {
            List<Role> roles = new ArrayList<>();
            for (String roleName : userDTO.getRoles()) {
                String trimmed = roleName.trim();
                if (!trimmed.isEmpty()) {
                    Role role = roleRepo.findByName(trimmed)
                            .orElseThrow(() -> new RuntimeException("Rôle non trouvé : " + trimmed));
                    roles.add(role);
                }
            }
            user.setRoles(roles);
        }

        userRepository.save(user);
    }
//    public List<User> getAllUsersNotAffectedToMember() {
//        return userRepository.findAll()
//                .stream()
//                .filter(user -> user.getMembre() == null)
//                .collect(Collectors.toList());
//    }
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * Activate or deactivate a user account by toggling the {@code enabled} flag.
     * A deactivated user keeps their data but is denied at login by
     * {@code CustomUserDetailsService}.
     */
    public UserDTO setUserEnabled(Long idUser, boolean enabled) {
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id : " + idUser));
        user.setEnabled(enabled);
        userRepository.save(user);
        return toUserDTO(user);
    }







}
