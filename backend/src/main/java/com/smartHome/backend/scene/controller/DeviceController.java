package com.smartHome.backend.scene.controller;

import com.smartHome.backend.scene.dto.DeviceCapabilityResponse;
import com.smartHome.backend.scene.dto.DeviceResponse;
import com.smartHome.backend.scene.Device;
import com.smartHome.backend.scene.DeviceCapability;
import com.smartHome.backend.scene.repository.DeviceRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@CrossOrigin(origins = "http://localhost:5173")
public class DeviceController {

    private final DeviceRepository deviceRepository;

    public DeviceController(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @GetMapping
    public ResponseEntity<List<DeviceResponse>> getAllDevices() {
        List<DeviceResponse> devices = deviceRepository.findAll()
                .stream()
                .map(this::toDeviceResponse)
                .toList();

        return ResponseEntity.ok(devices);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponse> getDevice(@PathVariable Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dispozitivul nu există"));

        return ResponseEntity.ok(toDeviceResponse(device));
    }

    private DeviceResponse toDeviceResponse(Device device) {
        List<DeviceCapabilityResponse> capabilities = device.getCapabilities()
                .stream()
                .map(this::toCapabilityResponse)
                .toList();

        return new DeviceResponse(
                device.getId(),
                device.getName(),
                device.getRoom(),
                device.getType(),
                device.getIntegrationType(),
                device.getIdentifier(),
                device.isEnabled(),
                capabilities
        );
    }

    private DeviceCapabilityResponse toCapabilityResponse(DeviceCapability capability) {
        return new DeviceCapabilityResponse(
                capability.getId(),
                capability.getCapability(),
                capability.getMinValue(),
                capability.getMaxValue(),
                capability.getUnit()
        );
    }
}