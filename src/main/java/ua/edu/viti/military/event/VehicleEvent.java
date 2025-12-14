package ua.edu.viti.military.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import ua.edu.viti.military.entity.Driver;
import ua.edu.viti.military.entity.Vehicle;
import ua.edu.viti.military.entity.VehicleMovementType;
import ua.edu.viti.military.entity.VehicleStatus;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class VehicleEvent extends ApplicationEvent {

    private final Long vehicleId;
    private final String registrationNumber;
    private final VehicleMovementType type;
    private final VehicleStatus previousStatus;
    private final VehicleStatus newStatus;
    private final String driverName;
    private final String performedBy;
    private final LocalDateTime occurredAt;
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
        this.driverName = buildDriverFullName(vehicle.getDriver());
        this.performedBy = performedBy;
        this.occurredAt = LocalDateTime.now();
        this.notes = notes;
    }

    private String buildDriverFullName(Driver driver) {
        if (driver == null) {
            return null;
        }

        String fullName = String.join(" ",
                Objects.toString(driver.getLastName(), ""),
                Objects.toString(driver.getFirstName(), ""),
                Objects.toString(driver.getMiddleName(), ""))
                .replaceAll("\\s+", " ")
                .trim();

        return fullName.isEmpty() ? null : fullName;
    }
}
