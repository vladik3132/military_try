package ua.edu.viti.military.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import ua.edu.viti.military.entity.VehicleMovementType;
import ua.edu.viti.military.repository.VehicleRepository;
import ua.edu.viti.military.service.MetricsService;

@Component
@RequiredArgsConstructor
@Slf4j
public class VehicleEventListener {

    private final VehicleRepository vehicleRepository;
    private final MetricsService metricsService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleVehicleEvent(VehicleEvent event) {
        log.info(
                "Processing vehicle event: vehicleId={}, regNumber={}, type={}, by={}",
                event.getVehicleId(),
                event.getRegistrationNumber(),
                event.getType(),
                event.getPerformedBy()
        );

        metricsService.recordEventObserved();

        if (event.getType() == VehicleMovementType.SEND_TO_MAINTENANCE) {
            log.info("Triggering maintenance notification for vehicle {}", event.getRegistrationNumber());
            simulateSlowNotification();
        }

        boolean driverChange = event.getType() == VehicleMovementType.ASSIGN_DRIVER
                || event.getType() == VehicleMovementType.UNASSIGN_DRIVER;
        if (driverChange) {
            log.info("Updating cached driver assignment info for vehicle {}", event.getRegistrationNumber());
            simulateSlowNotification();
        }

        if (event.getType() == VehicleMovementType.RETURN_FROM_MAINTENANCE) {
            log.info("Recalculating availability statistics for vehicle {}", event.getRegistrationNumber());
            simulateSlowNotification();
        }

        vehicleRepository.findById(event.getVehicleId()).ifPresent(vehicle ->
                log.debug("Post-event vehicle status: {}", vehicle.getStatus())
        );
    }

    @EventListener
    public void logLowStockPlaceholder(VehicleEvent event) {
        log.trace("Vehicle event observed (sync): {} {}", event.getType(), event.getRegistrationNumber());
    }

    private void simulateSlowNotification() {
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
