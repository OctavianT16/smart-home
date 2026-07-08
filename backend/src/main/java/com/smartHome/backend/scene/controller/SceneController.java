package com.smartHome.backend.scene.controller;

import com.smartHome.backend.scene.dto.CreateSceneRequest;
import com.smartHome.backend.scene.dto.SceneResponse;
import com.smartHome.backend.scene.dto.SceneSummaryResponse;
import com.smartHome.backend.scene.service.SceneService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scenes")
//@CrossOrigin(origins = "http://localhost:5173")
@CrossOrigin(origins = "*")
public class SceneController {

    private final SceneService sceneService;

    public SceneController(SceneService sceneService) {
        this.sceneService = sceneService;
    }

    @GetMapping
    public ResponseEntity<List<SceneSummaryResponse>> getAllScenes() {
        return ResponseEntity.ok(sceneService.getSceneSummaries());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SceneResponse> getScene(@PathVariable Long id) {
        return ResponseEntity.ok(sceneService.getScene(id));
    }

    @PostMapping
    public ResponseEntity<SceneResponse> createScene(@RequestBody CreateSceneRequest request) {
        SceneResponse scene = sceneService.createScene(request);
        return ResponseEntity.ok(scene);
    }

    @PostMapping("/{id}/run")
    public ResponseEntity<Void> runScene(@PathVariable Long id) {
        sceneService.runScene(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScene(@PathVariable Long id) {
        sceneService.deleteScene(id);
        return ResponseEntity.noContent().build();
    }
}