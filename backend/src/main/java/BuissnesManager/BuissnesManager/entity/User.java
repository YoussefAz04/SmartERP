package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
@ToString
@Table(name = "App_User")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ✅ CHANGED FROM AUTO to IDENTITY
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;
    
    @Column(length = 20)
    private String phone;

    @Column(unique = true, nullable = false, length = 100)
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(length = 255)
    private String password;

    @Column(length = 500)
    private String imageUrl;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @JsonIgnore
    private List<Role> roles;

    @Column(nullable = false)
    private boolean enabled = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id")
    @JsonIgnore
    private Entreprise entreprise;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, insertable = true, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;

    @JsonIgnore
    @Column(name = "reset_password_token", length = 100)
    private String resetPasswordToken;

    @JsonIgnore
    @Column(name = "reset_password_token_expiry")
    private LocalDateTime resetPasswordTokenExpiry;
    
    // =====================
    // CONSTRUCTOR
    // =====================
    
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.enabled = true;
    }
}