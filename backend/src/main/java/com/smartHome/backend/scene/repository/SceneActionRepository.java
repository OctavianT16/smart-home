package com.smartHome.backend.scene.repository;

import com.smartHome.backend.scene.SceneAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SceneActionRepository extends JpaRepository<SceneAction, Long> {

    List<SceneAction> findBySceneIdOrderByExecutionOrderAsc(Long sceneId);
}