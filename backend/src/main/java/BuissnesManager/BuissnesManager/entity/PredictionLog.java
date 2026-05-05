package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Persisted log of every AI sales forecast issued for an enterprise so the
 * platform can later compare it against the real revenue (CA) of the target
 * month. Storing predictions and only reconciling them once reality is known
 * is a basic but important guard against survivorship bias and lets the UI
 * advertise the model's track record honestly.
 */
@Entity
@Table(
    name = "prediction_log",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_prediction_log_entreprise_target_horizon",
            columnNames = {"entreprise_id", "prediction_month", "target_month", "horizon_months"}
        )
    },
    indexes = {
        @Index(name = "idx_prediction_log_entreprise", columnList = "entreprise_id"),
        @Index(name = "idx_prediction_log_target_month", columnList = "target_month")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PredictionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Entreprise entreprise;

    /** Date the prediction was made (forecast issue date). */
    @Column(name = "prediction_date", nullable = false)
    private LocalDate predictionDate;

    /** First day of the calendar month in which the prediction was issued. */
    @Column(name = "prediction_month", nullable = false)
    private LocalDate predictionMonth;

    /** First day of the calendar month the prediction is for. */
    @Column(name = "target_month", nullable = false)
    private LocalDate targetMonth;

    /** Forecast horizon in months (1 = next month, 2 = m+2, …). */
    @Column(name = "horizon_months", nullable = false)
    private Integer horizonMonths;

    @Column(name = "predicted_value")
    private Double predictedValue;

    @Column(name = "lower_bound")
    private Double lowerBound;

    @Column(name = "upper_bound")
    private Double upperBound;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "methodology", length = 512)
    private String methodology;

    @Column(name = "model_name", length = 128)
    private String modelName;

    /** Runner-up model name (for A/B reporting once reconciled). */
    @Column(name = "challenger_model", length = 128)
    private String challengerModel;

    /** Walk-forward MASE at issue time (for drift / A/B reports). */
    @Column(name = "cv_mase")
    private Double cvMase;

    /** Walk-forward sMAPE at issue time. */
    @Column(name = "cv_smape")
    private Double cvSmape;

    /** Box-Cox λ used at issue time, if the transform was enabled. */
    @Column(name = "boxcox_lambda")
    private Double boxCoxLambda;

    /** Feature spec name (e.g. {@code "SMALL"} or {@code "WIDE+CAL"}) at issue time. */
    @Column(name = "feature_spec", length = 64)
    private String featureSpec;

    /** Number of training rows actually used. */
    @Column(name = "data_points_used")
    private Integer dataPointsUsed;

    /** Outliers detected by the Hampel filter at issue time. */
    @Column(name = "outliers_detected")
    private Integer outliersDetected;

    /** Actual realised CA for the target month. Filled by reconciliation once the
     * month is over. */
    @Column(name = "actual_value")
    private Double actualValue;

    /** |predicted − actual|. Filled at reconciliation. */
    @Column(name = "absolute_error")
    private Double absoluteError;

    /** |predicted − actual| / max(|actual|, ε). Filled at reconciliation. */
    @Column(name = "percent_error")
    private Double percentError;

    /** True if the actual value fell inside [lowerBound, upperBound]. */
    @Column(name = "within_interval")
    private Boolean withinInterval;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
