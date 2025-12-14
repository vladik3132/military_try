package ua.edu.viti.military.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.viti.military.dto.request.VehicleCategoryCreateRequest;
import ua.edu.viti.military.dto.request.VehicleCategoryUpdateRequest;
import ua.edu.viti.military.dto.response.VehicleCategoryResponse;
import ua.edu.viti.military.entity.VehicleCategory;
import ua.edu.viti.military.exception.DuplicateResourceException;
import ua.edu.viti.military.exception.ResourceNotFoundException;
import ua.edu.viti.military.mapper.VehicleCategoryMapper;
import ua.edu.viti.military.repository.VehicleCategoryRepository;
import ua.edu.viti.military.service.VehicleCategoryService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VehicleCategoryServiceImpl implements VehicleCategoryService {

    private final VehicleCategoryRepository vehicleCategoryRepository;
    private final VehicleCategoryMapper vehicleCategoryMapper;

    @Override
    @Transactional
    @CacheEvict(value = "vehicleCategories", allEntries = true)
    public VehicleCategoryResponse create(VehicleCategoryCreateRequest request) {
        log.info("Creating vehicle category: {}", request.getName());

        if (vehicleCategoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Категорія з назвою " + request.getName() + " вже існує");
        }

        if (vehicleCategoryRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Категорія з кодом " + request.getCode() + " вже існує");
        }

        VehicleCategory category = vehicleCategoryMapper.toEntity(request);

        VehicleCategory saved = vehicleCategoryRepository.save(category);
        log.info("Vehicle category created with ID: {}", saved.getId());
        
        return vehicleCategoryMapper.toResponse(saved);
    }

    @Override
    @Cacheable(value = "vehicleCategories", key = "#id")
    public VehicleCategoryResponse getById(Long id) {
        log.debug("Fetching vehicle category with ID: {}", id);
        
        VehicleCategory category = vehicleCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VehicleCategory not found: " + id));
        
        return vehicleCategoryMapper.toResponse(category);
    }

    @Override
    @Cacheable(value = "vehicleCategories", key = "'all'")
    public List<VehicleCategoryResponse> getAll() {
        log.debug("Fetching all vehicle categories");
        
        return vehicleCategoryMapper.toResponseList(vehicleCategoryRepository.findAll());
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "vehicleCategories", key = "#id"),
            @CacheEvict(value = "vehicleCategories", key = "'all'")
    })
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

        vehicleCategoryMapper.updateEntityFromDto(request, category);

        VehicleCategory saved = vehicleCategoryRepository.save(category);
        log.info("Vehicle category with ID {} updated successfully", id);
        
        return vehicleCategoryMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "vehicleCategories", key = "#id"),
            @CacheEvict(value = "vehicleCategories", allEntries = true)
    })
    public void delete(Long id) {
        log.info("Deleting vehicle category with ID: {}", id);
        
        if (!vehicleCategoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("VehicleCategory not found: " + id);
        }
        
        vehicleCategoryRepository.deleteById(id);
        log.info("Vehicle category with ID {} deleted successfully", id);
    }

}
