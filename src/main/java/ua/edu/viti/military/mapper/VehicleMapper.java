package ua.edu.viti.military.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ua.edu.viti.military.dto.request.VehicleCreateRequest;
import ua.edu.viti.military.dto.request.VehicleUpdateRequest;
import ua.edu.viti.military.dto.response.VehicleResponse;
import ua.edu.viti.military.entity.Driver;
import ua.edu.viti.military.entity.Vehicle;

import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring")
public interface VehicleMapper {

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "categoryCode", source = "category.code")
    @Mapping(target = "driverId", source = "driver.id")
    @Mapping(target = "driverFullName", expression = "java(buildDriverFullName(vehicle.getDriver()))")
    VehicleResponse toResponse(Vehicle vehicle);

    List<VehicleResponse> toResponseList(List<Vehicle> vehicles);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "fuelType", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Vehicle toEntity(VehicleCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "fuelType", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(VehicleUpdateRequest request, @MappingTarget Vehicle entity);

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

