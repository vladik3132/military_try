package ua.edu.viti.military.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
@Slf4j
public class MetricsService {

    private final Counter driverAssignments;
    private final Counter driverUnassignments;
    private final Counter maintenanceStarts;
    private final Counter maintenanceCompletions;
    private final Counter vehicleEvents;
    private final Timer movementTimer;

    public MetricsService(MeterRegistry meterRegistry) {
        this.driverAssignments = Counter.builder("vehicle.driver.assigned")
                .description("Number of driver assignments")
                .register(meterRegistry);

        this.driverUnassignments = Counter.builder("vehicle.driver.unassigned")
                .description("Number of driver unassignments")
                .register(meterRegistry);

        this.maintenanceStarts = Counter.builder("vehicle.maintenance.started")
                .description("Vehicles sent to maintenance")
                .register(meterRegistry);

        this.maintenanceCompletions = Counter.builder("vehicle.maintenance.completed")
                .description("Vehicles returned from maintenance")
                .register(meterRegistry);

        this.vehicleEvents = Counter.builder("vehicle.movement.events")
                .description("Observed vehicle movement events")
                .register(meterRegistry);

        this.movementTimer = Timer.builder("vehicle.movement.duration")
                .description("Duration of vehicle movement operations")
                .register(meterRegistry);
    }

    public void recordAssignment() {
        driverAssignments.increment();
    }

    public void recordUnassignment() {
        driverUnassignments.increment();
    }

    public void recordMaintenanceStart() {
        maintenanceStarts.increment();
    }

    public void recordMaintenanceCompletion() {
        maintenanceCompletions.increment();
    }

    public void recordEventObserved() {
        vehicleEvents.increment();
    }

    public <T> T measureMovementOperation(Supplier<T> operation) {
        return movementTimer.record(operation);
    }

    public void recordDuration(Runnable runnable) {
        Timer.Sample sample = Timer.start();
        try {
            runnable.run();
        } finally {
            sample.stop(movementTimer);
        }
    }
}
