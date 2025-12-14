package ua.edu.viti.military.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.edu.viti.military.entity.VehicleMovement;
import ua.edu.viti.military.entity.VehicleMovementType;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VehicleMovementRepository extends JpaRepository<VehicleMovement, Long> {

    List<VehicleMovement> findByVehicleIdOrderByPerformedAtDesc(Long vehicleId);

    List<VehicleMovement> findByTypeAndPerformedAtBetween(
            VehicleMovementType type,
            LocalDateTime start,
            LocalDateTime end
    );
}
