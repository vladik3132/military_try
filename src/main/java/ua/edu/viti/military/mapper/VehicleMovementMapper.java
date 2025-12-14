package ua.edu.viti.military.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ua.edu.viti.military.dto.response.VehicleMovementResponse;
import ua.edu.viti.military.entity.Driver;
import ua.edu.viti.military.entity.VehicleMovement;

import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring")
public interface VehicleMovementMapper {

    @Mapping(target = "vehicleId", source = "vehicle.id")
    @Mapping(target = "vehicleModel", source = "vehicle.model")
    @Mapping(target = "registrationNumber", source = "vehicle.registrationNumber")
    @Mapping(target = "driverId", source = "driver.id")
    @Mapping(target = "driverFullName", expression = "java(buildDriverFullName(movement.getDriver()))")
    VehicleMovementResponse toResponse(VehicleMovement movement);

    List<VehicleMovementResponse> toResponseList(List<VehicleMovement> movements);

    default String buildDriverFullName(Driver driver) {
        if (driver == null) {
            return null;
        }
        String fullName = String.join(" ",
                Objects.toString(driver.getLastName(), ""),
                Objects.toString(driver.getFirstName(), "")).trim();
        return fullName.isEmpty() ? null : fullName;
    }
}

