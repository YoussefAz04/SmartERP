package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.UserRequest;
import BuissnesManager.BuissnesManager.entity.Entreprise;
import BuissnesManager.BuissnesManager.entity.Role;
import BuissnesManager.BuissnesManager.entity.User;
import BuissnesManager.BuissnesManager.entity.UserDTO;
import BuissnesManager.BuissnesManager.repository.EntrepriseRepo;
import BuissnesManager.BuissnesManager.repository.RoleRepo;
import BuissnesManager.BuissnesManager.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    private RoleRepo roleRepo ;

    public User createUser(UserRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Un utilisateur avec ce nom existe déjà.");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);

        return userRepository.save(user);
    }
    public User createUserWithEntrepriseAndEquipe(UserRequest request, Long idEntreprise,Long idEquipe) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Un utilisateur avec ce nom existe déjà.");
        }



        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);

        user.setEntreprise(entrepriseRepo.findById(idEntreprise).get());
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
        return users.stream().map(user -> {
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
            }

            return dto;
        }).collect(Collectors.toList());
    }


    public UserDTO getUserById(Long idUser){
        User user = userRepository.findById(idUser).get();
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
        User user = userRepository.findById(idUser).get();
        user.setUsername(userDTO.getUsername());
        user.setPhone(userDTO.getPhone());
        Entreprise entreprise = entrepriseRepo.findById(userDTO.getEntrepriseId())
                .orElseThrow(() -> new RuntimeException("Entreprise non trouvée"));
        user.setEntreprise(entreprise);
        user.setEnabled(userDTO.isEnabled());

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







}
