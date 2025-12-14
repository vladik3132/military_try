package ua.edu.viti.military.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.viti.military.dto.request.VehicleCategoryCreateRequest;
import ua.edu.viti.military.dto.request.VehicleCategoryUpdateRequest;
import ua.edu.viti.military.dto.response.VehicleCategoryResponse;
import ua.edu.viti.military.entity.VehicleCategory;
import ua.edu.viti.military.exception.DuplicateResourceException;
import ua.edu.viti.military.exception.ResourceNotFoundException;
import ua.edu.viti.military.repository.VehicleCategoryRepository;
import ua.edu.viti.military.service.VehicleCategoryService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VehicleCategoryServiceImpl implements VehicleCategoryService {

    private final VehicleCategoryRepository vehicleCategoryRepository;

    @Override
    @Transactional
    public VehicleCategoryResponse create(VehicleCategoryCreateRequest request) {
        log.info("Creating vehicle category: {}", request.getName());

        if (vehicleCategoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Категорія з назвою " + request.getName() + " вже існує");
        }

        if (vehicleCategoryRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Категорія з кодом " + request.getCode() + " вже існує");
        }

        VehicleCategory category = new VehicleCategory();
        category.setName(request.getName());
        category.setCode(request.getCode());
        category.setDescription(request.getDescription());
        category.setRequiredLicense(request.getRequiredLicense());
        category.setMaxLoadCapacity(request.getMaxLoadKg());
        category.setSeats(request.getSeats());

        VehicleCategory saved = vehicleCategoryRepository.save(category);
        log.info("Vehicle category created with ID: {}", saved.getId());
        
        return toResponse(saved);
    }

    @Override
    public VehicleCategoryResponse getById(Long id) {
        log.debug("Fetching vehicle category with ID: {}", id);
        
        VehicleCategory category = vehicleCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VehicleCategory not found: " + id));
        
        return toResponse(category);
    }

    @Override
    public List<VehicleCategoryResponse> getAll() {
        log.debug("Fetching all vehicle categories");
        
        return vehicleCategoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VehicleCategoryResponse update(Long id, VehicleCategoryUpdateRequest request) {
        log.info("Updating vehicle category with ID: {}", id);
        
        VehicleCategory category = vehicleCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VehicleCategory not found: " + id));

        if (request.getName() != null) {
            vehicleCategoryRepository.findByName(request.getName())
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new DuplicateResourceException("Категорія з назвою " + request.getName() + " вже існує");
                    });
            category.setName(request.getName());
        }

        if (request.getCode() != null) {
            vehicleCategoryRepository.findByCode(request.getCode())
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new DuplicateResourceException("Категорія з кодом " + request.getCode() + " вже існує");
                    });
            category.setCode(request.getCode());
        }

        if (request.getDescription() != null) category.setDescription(request.getDescription());
        if (request.getRequiredLicense() != null) category.setRequiredLicense(request.getRequiredLicense());
        if (request.getMaxLoadKg() != null) category.setMaxLoadCapacity(request.getMaxLoadKg());
        if (request.getSeats() != null) category.setSeats(request.getSeats());

        VehicleCategory saved = vehicleCategoryRepository.save(category);
        log.info("Vehicle category with ID {} updated successfully", id);
        
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting vehicle category with ID: {}", id);
        
        if (!vehicleCategoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("VehicleCategory not found: " + id);
        }
        
        vehicleCategoryRepository.deleteById(id);
        log.info("Vehicle category with ID {} deleted successfully", id);
    }

    private VehicleCategoryResponse toResponse(VehicleCategory c) {
        VehicleCategoryResponse r = new VehicleCategoryResponse();
        r.setId(c.getId());
        r.setName(c.getName());
        r.setCode(c.getCode());
        r.setDescription(c.getDescription());
        r.setRequiredLicense(c.getRequiredLicense());
        r.setMaxLoadCapacity(c.getMaxLoadCapacity());
        r.setSeats(c.getSeats());
        r.setCreatedAt(c.getCreatedAt());
        r.setUpdatedAt(c.getUpdatedAt());
        return r;
    }
}
