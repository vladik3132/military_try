package ua.edu.viti.military.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.viti.military.dto.request.VehicleDriverAssignmentRequest;
import ua.edu.viti.military.dto.request.VehicleMaintenanceCompletionRequest;
import ua.edu.viti.military.dto.request.VehicleMovementRequest;
import ua.edu.viti.military.dto.response.VehicleMovementResponse;
import ua.edu.viti.military.entity.Driver;
import ua.edu.viti.military.entity.Vehicle;
import ua.edu.viti.military.entity.VehicleMovement;
import ua.edu.viti.military.entity.VehicleMovementType;
import ua.edu.viti.military.entity.VehicleStatus;
import ua.edu.viti.military.exception.BusinessLogicException;
import ua.edu.viti.military.exception.ResourceNotFoundException;
import ua.edu.viti.military.event.VehicleEvent;
import ua.edu.viti.military.mapper.VehicleMovementMapper;
import ua.edu.viti.military.repository.DriverRepository;
import ua.edu.viti.military.repository.VehicleMovementRepository;
import ua.edu.viti.military.repository.VehicleRepository;
import ua.edu.viti.military.service.VehicleMovementService;
import ua.edu.viti.military.service.MetricsService;

import java.time.LocalDate;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.MDC;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VehicleMovementServiceImpl implements VehicleMovementService {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final VehicleMovementRepository vehicleMovementRepository;
    private final VehicleMovementMapper vehicleMovementMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final MetricsService metricsService;

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public VehicleMovementResponse assignDriver(VehicleDriverAssignmentRequest request) {
        return runObservedOperation(request.getVehicleId(), "assign_driver", () -> {
            log.info("Assigning driver {} to vehicle {}", request.getDriverId(), request.getVehicleId());

            Vehicle vehicle = findVehicleForUpdate(request.getVehicleId());
            Driver driver = driverRepository.findById(request.getDriverId())
                    .orElseThrow(() -> new ResourceNotFoundException("Водія не знайдено"));

            if (vehicle.getStatus() == VehicleStatus.WRITTEN_OFF) {
                throw new BusinessLogicException("Неможливо призначити водія списаному транспорту");
            }

            VehicleStatus previousStatus = vehicle.getStatus();
            vehicle.setDriver(driver);
            Vehicle saved = vehicleRepository.save(vehicle);

            VehicleMovement movement = buildMovement(saved, driver, VehicleMovementType.ASSIGN_DRIVER,
                    previousStatus, saved.getStatus(), request.getNotes());
            VehicleMovement persisted = vehicleMovementRepository.save(movement);
            publishEvent(saved, movement);
            metricsService.recordAssignment();
            return vehicleMovementMapper.toResponse(persisted);
        });
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public VehicleMovementResponse unassignDriver(VehicleMovementRequest request) {
        return runObservedOperation(request.getVehicleId(), "unassign_driver", () -> {
            log.info("Unassigning driver from vehicle {}", request.getVehicleId());

            Vehicle vehicle = findVehicleForUpdate(request.getVehicleId());
            if (vehicle.getDriver() == null) {
                throw new BusinessLogicException("У транспорту немає призначеного водія");
            }

            Driver previousDriver = vehicle.getDriver();
            VehicleStatus previousStatus = vehicle.getStatus();

            vehicle.setDriver(null);
            Vehicle saved = vehicleRepository.save(vehicle);

            VehicleMovement movement = buildMovement(saved, previousDriver, VehicleMovementType.UNASSIGN_DRIVER,
                    previousStatus, saved.getStatus(), request.getNotes());
            VehicleMovement persisted = vehicleMovementRepository.save(movement);
            publishEvent(saved, movement);
            metricsService.recordUnassignment();
            return vehicleMovementMapper.toResponse(persisted);
        });
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public VehicleMovementResponse sendToMaintenance(VehicleMovementRequest request) {
        return runObservedOperation(request.getVehicleId(), "send_to_maintenance", () -> {
            log.info("Sending vehicle {} to maintenance", request.getVehicleId());

            Vehicle vehicle = findVehicleForUpdate(request.getVehicleId());
            if (vehicle.getStatus() == VehicleStatus.IN_MAINTENANCE) {
                throw new BusinessLogicException("Транспорт вже на технічному обслуговуванні");
            }
            if (vehicle.getStatus() == VehicleStatus.WRITTEN_OFF) {
                throw new BusinessLogicException("Списаний транспорт не можна відправити на ТО");
            }

            VehicleStatus previousStatus = vehicle.getStatus();
            vehicle.setStatus(VehicleStatus.IN_MAINTENANCE);
            Vehicle saved = vehicleRepository.save(vehicle);

            VehicleMovement movement = buildMovement(saved, saved.getDriver(), VehicleMovementType.SEND_TO_MAINTENANCE,
                    previousStatus, saved.getStatus(), request.getNotes());
            VehicleMovement persisted = vehicleMovementRepository.save(movement);
            publishEvent(saved, movement);
            metricsService.recordMaintenanceStart();
            return vehicleMovementMapper.toResponse(persisted);
        });
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public VehicleMovementResponse completeMaintenance(VehicleMaintenanceCompletionRequest request) {
        return runObservedOperation(request.getVehicleId(), "complete_maintenance", () -> {
            log.info("Completing maintenance for vehicle {}", request.getVehicleId());

            Vehicle vehicle = findVehicleForUpdate(request.getVehicleId());
            if (vehicle.getStatus() != VehicleStatus.IN_MAINTENANCE) {
                throw new BusinessLogicException("Транспорт не перебуває на технічному обслуговуванні");
            }

            VehicleStatus previousStatus = vehicle.getStatus();
            vehicle.setStatus(VehicleStatus.OPERATIONAL);
            vehicle.setLastMaintenanceDate(LocalDate.now());
            if (request.getMileage() != null) {
                if (vehicle.getMileage() != null && request.getMileage() < vehicle.getMileage()) {
                    throw new BusinessLogicException("Пробіг після ТО не може бути меншим за поточний");
                }
                vehicle.setMileage(request.getMileage());
                vehicle.setLastMaintenanceMileage(request.getMileage());
            } else if (vehicle.getMileage() != null) {
                vehicle.setLastMaintenanceMileage(vehicle.getMileage());
            }
            if (request.getNextMaintenanceDate() != null) {
                vehicle.setNextMaintenanceDate(request.getNextMaintenanceDate());
            }

            Vehicle saved = vehicleRepository.save(vehicle);

            VehicleMovement movement = buildMovement(saved, saved.getDriver(), VehicleMovementType.RETURN_FROM_MAINTENANCE,
                    previousStatus, saved.getStatus(), request.getNotes());
            VehicleMovement persisted = vehicleMovementRepository.save(movement);
            publishEvent(saved, movement);
            metricsService.recordMaintenanceCompletion();
            return vehicleMovementMapper.toResponse(persisted);
        });
    }

    @Override
    public List<VehicleMovementResponse> getVehicleHistory(Long vehicleId) {
        return vehicleMovementMapper.toResponseList(
                vehicleMovementRepository.findByVehicleIdOrderByPerformedAtDesc(vehicleId)
        );
    }

    private Vehicle findVehicleForUpdate(Long vehicleId) {
        return vehicleRepository.findByIdForUpdate(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Транспорт з ID " + vehicleId + " не знайдено"));
    }

    private VehicleMovement buildMovement(
            Vehicle vehicle,
            Driver driver,
            VehicleMovementType type,
            VehicleStatus previousStatus,
            VehicleStatus newStatus,
            String notes
    ) {
        VehicleMovement movement = new VehicleMovement();
        movement.setVehicle(vehicle);
        movement.setDriver(driver);
        movement.setType(type);
        movement.setPreviousStatus(previousStatus);
        movement.setNewStatus(newStatus);
        movement.setNotes(notes);
        movement.setPerformedBy(getCurrentUser());
        return movement;
    }

    private void publishEvent(Vehicle vehicle, VehicleMovement movement) {
        eventPublisher.publishEvent(new VehicleEvent(
                this,
                vehicle,
                movement.getType(),
                movement.getPreviousStatus(),
                movement.getNewStatus(),
                movement.getPerformedBy(),
                movement.getNotes()
        ));
    }

    private <T> T runObservedOperation(Long vehicleId, String operation, Supplier<T> operationLogic) {
        MDC.put("operation", operation);
        if (vehicleId != null) {
            MDC.put("vehicleId", vehicleId.toString());
        }
        MDC.put("userId", getCurrentUser());
        try {
            return metricsService.measureMovementOperation(operationLogic);
        } finally {
            MDC.remove("operation");
            MDC.remove("vehicleId");
            MDC.remove("userId");
        }
    }

    private String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }
}
