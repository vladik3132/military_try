package ua.edu.viti.military.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.edu.viti.military.dto.request.VehicleDriverAssignmentRequest;
import ua.edu.viti.military.dto.request.VehicleMaintenanceCompletionRequest;
import ua.edu.viti.military.dto.request.VehicleMovementRequest;
import ua.edu.viti.military.dto.response.VehicleMovementResponse;
import ua.edu.viti.military.service.VehicleMovementService;

import java.util.List;

@RestController
@RequestMapping("/api/vehicle-movements")
@RequiredArgsConstructor
@Tag(name = "Vehicle Movements", description = "Журнал операцій з транспортом та транзакційні дії")
public class VehicleMovementController {

    private final VehicleMovementService vehicleMovementService;

    @PostMapping("/assign-driver")
    @Operation(summary = "Призначити водія транспорту")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<VehicleMovementResponse> assignDriver(
            @Valid @RequestBody VehicleDriverAssignmentRequest request
    ) {
        VehicleMovementResponse response = vehicleMovementService.assignDriver(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/unassign-driver")
    @Operation(summary = "Зняти призначення водія")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<VehicleMovementResponse> unassignDriver(
            @Valid @RequestBody VehicleMovementRequest request
    ) {
        VehicleMovementResponse response = vehicleMovementService.unassignDriver(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/send-to-maintenance")
    @Operation(summary = "Відправити транспорт на технічне обслуговування")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<VehicleMovementResponse> sendToMaintenance(
            @Valid @RequestBody VehicleMovementRequest request
    ) {
        VehicleMovementResponse response = vehicleMovementService.sendToMaintenance(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/complete-maintenance")
    @Operation(summary = "Повернути транспорт з технічного обслуговування")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<VehicleMovementResponse> completeMaintenance(
            @Valid @RequestBody VehicleMaintenanceCompletionRequest request
    ) {
        VehicleMovementResponse response = vehicleMovementService.completeMaintenance(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/vehicle/{vehicleId}")
    @Operation(summary = "Отримати історію операцій по транспорту")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    public ResponseEntity<List<VehicleMovementResponse>> getVehicleHistory(@PathVariable Long vehicleId) {
        List<VehicleMovementResponse> history = vehicleMovementService.getVehicleHistory(vehicleId);
        return ResponseEntity.ok(history);
    }
}
