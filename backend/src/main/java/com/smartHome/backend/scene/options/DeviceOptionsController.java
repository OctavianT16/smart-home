package com.smartHome.backend.scene.options;

import com.smartHome.backend.scene.options.DeviceOptionsResponse;
import com.smartHome.backend.scene.options.DeviceOptionsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/device-options")
@CrossOrigin(origins = "http://localhost:5173")
public class DeviceOptionsController {

    private final DeviceOptionsService deviceOptionsService;

    public DeviceOptionsController(DeviceOptionsService deviceOptionsService) {
        this.deviceOptionsService = deviceOptionsService;
    }

    @GetMapping("/{deviceId}/commands")
    public ResponseEntity<DeviceOptionsResponse> getCommandOptionsForDevice(@PathVariable Long deviceId) {
        return ResponseEntity.ok(deviceOptionsService.getCommandOptionsForDevice(deviceId));
    }
}