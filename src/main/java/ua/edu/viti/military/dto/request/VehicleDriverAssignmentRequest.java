package ua.edu.viti.military.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VehicleDriverAssignmentRequest {

    @NotNull(message = "ID транспорту обов'язковий")
    @Positive
    private Long vehicleId;

    @NotNull(message = "ID водія обов'язковий")
    @Positive
    private Long driverId;

    @Size(max = 500)
    private String notes;
}
