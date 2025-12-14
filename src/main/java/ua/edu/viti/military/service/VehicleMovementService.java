package ua.edu.viti.military.service;

import ua.edu.viti.military.dto.request.VehicleDriverAssignmentRequest;
import ua.edu.viti.military.dto.request.VehicleMaintenanceCompletionRequest;
import ua.edu.viti.military.dto.request.VehicleMovementRequest;
import ua.edu.viti.military.dto.response.VehicleMovementResponse;

import java.util.List;

public interface VehicleMovementService {
    VehicleMovementResponse assignDriver(VehicleDriverAssignmentRequest request);

    VehicleMovementResponse unassignDriver(VehicleMovementRequest request);

    VehicleMovementResponse sendToMaintenance(VehicleMovementRequest request);

    VehicleMovementResponse completeMaintenance(VehicleMaintenanceCompletionRequest request);

    List<VehicleMovementResponse> getVehicleHistory(Long vehicleId);
}
