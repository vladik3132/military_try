package ua.edu.viti.military.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class VehicleMaintenanceCompletionRequest {

    @NotNull(message = "ID транспорту обов'язковий")
    @Positive
    private Long vehicleId;

    @Positive(message = "Пробіг має бути додатнім")
    private Integer mileage;

    private LocalDate nextMaintenanceDate;

    @Size(max = 500)
    private String notes;
}
