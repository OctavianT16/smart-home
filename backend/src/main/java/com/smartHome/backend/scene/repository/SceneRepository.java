package com.smartHome.backend.scene.repository;

import com.smartHome.backend.scene.Scene;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SceneRepository extends JpaRepository<Scene, Long> {
}

