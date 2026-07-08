package com.smartHome.backend.lights.esp32Flower;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flower")
public class FlowerController {

    private final FlowerCommandService flowerCommandService;

    public FlowerController(FlowerCommandService flowerCommandService) {
        this.flowerCommandService = flowerCommandService;
    }

    @PostMapping("/power/**")
    public void power(@RequestParam(defaultValue = "esp32-living") String deviceId,
                      @RequestParam boolean on) {
        flowerCommandService.setPower(deviceId, on);
    }

    @PostMapping("/brightness")
    public void brightness(@RequestParam(defaultValue = "esp32-living") String deviceId,
                           @RequestParam int brightness) {
        int val255 = flowerCommandService.pctTo255(brightness);
        flowerCommandService.setBrightness(deviceId, brightness);
    }
}