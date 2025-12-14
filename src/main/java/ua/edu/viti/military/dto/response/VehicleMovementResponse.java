package ua.edu.viti.military.dto.response;

import lombok.Data;
import ua.edu.viti.military.entity.VehicleMovementType;
import ua.edu.viti.military.entity.VehicleStatus;

import java.time.LocalDateTime;

@Data
public class VehicleMovementResponse {
    private Long id;
    private Long vehicleId;
    private String vehicleModel;
    private String registrationNumber;
    private VehicleMovementType type;
    private VehicleStatus previousStatus;
    private VehicleStatus newStatus;
    private Long driverId;
    private String driverFullName;
    private String notes;
    private String performedBy;
    private LocalDateTime performedAt;
}
