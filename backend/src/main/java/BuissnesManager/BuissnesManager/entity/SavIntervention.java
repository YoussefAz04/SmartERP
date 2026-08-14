package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sav_intervention")
@Getter
@Setter
@NoArgsConstructor
public class SavIntervention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    @JsonIgnore
    private SavTicket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_user_id")
    @JsonIgnore
    private User technician;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(precision = 15, scale = 2)
    private BigDecimal laborCost;

    @Column(precision = 15, scale = 2)
    private BigDecimal partsCost;

    @CreationTimestamp
    @Column(name = "performed_at", nullable = false, updatable = false)
    private LocalDateTime performedAt;
}
