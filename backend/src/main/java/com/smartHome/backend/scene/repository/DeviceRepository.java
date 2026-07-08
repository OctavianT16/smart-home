package com.smartHome.backend.scene.repository;

import com.smartHome.backend.scene.Device;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, Long> {
}

