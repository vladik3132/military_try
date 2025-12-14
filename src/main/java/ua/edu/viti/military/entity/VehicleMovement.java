package ua.edu.viti.military.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_movements")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private VehicleMovementType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 20)
    private VehicleStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", length = 20)
    private VehicleStatus newStatus;

    @Column(length = 500)
    private String notes;

    @Column(name = "performed_by", length = 100)
    private String performedBy;

    @CreatedDate
    @Column(name = "performed_at", nullable = false, updatable = false)
    private LocalDateTime performedAt;

    @Version
    private Long version;
}
