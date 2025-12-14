package ua.edu.viti.military.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import ua.edu.viti.military.entity.Vehicle;
import ua.edu.viti.military.entity.VehicleMovementType;
import ua.edu.viti.military.entity.VehicleStatus;

import java.time.LocalDateTime;

@Getter
public class VehicleEvent extends ApplicationEvent {

    private final Long vehicleId;
    private final String registrationNumber;
    private final VehicleMovementType type;
    private final VehicleStatus previousStatus;
    private final VehicleStatus newStatus;
    private final String driverName;
    private final String performedBy;
    private final LocalDateTime timestamp;
    private final String notes;

    public VehicleEvent(
            Object source,
            Vehicle vehicle,
            VehicleMovementType type,
            VehicleStatus previousStatus,
            VehicleStatus newStatus,
            String performedBy,
            String notes
    ) {
        super(source);
        this.vehicleId = vehicle.getId();
        this.registrationNumber = vehicle.getRegistrationNumber();
        this.type = type;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.driverName = vehicle.getDriver() != null ? vehicle.getDriver().getFullName() : null;
        this.performedBy = performedBy;
        this.timestamp = LocalDateTime.now();
        this.notes = notes;
    }
}
