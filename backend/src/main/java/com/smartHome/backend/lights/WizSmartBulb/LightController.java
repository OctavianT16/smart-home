package com.smartHome.backend.lights.WizSmartBulb;


import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/light")
@CrossOrigin
public class LightController {

    private final LightService lightService;
    private final String ip = "192.168.0.122";
    public LightController(LightService lightService) {
        this.lightService = lightService;
    }

    @PostMapping("/on")
    public String turnOn() {
        String ip = this.ip;
        lightService.sendLightCommand(ip, true, null, null);
        return "Porneste becul - " + ip;
    }

    @PostMapping("/off")
    public String turnOff() {
        String ip = this.ip;
        lightService.sendLightCommand(ip, false, null, null);
        return "Opreste becul - " + ip;
    }

    @PostMapping("/brightness")
    public String setBrightness(@RequestParam int brightness) {
        String ip = this.ip;
        if (brightness < 10 || brightness > 100) {
            return "Luminozitatea trebuie sa fie între 10 and 100.";
        }
        lightService.sendLightCommand(ip, true, brightness, null);
        return "Luminozitate setata la " + brightness + "% pentru " + ip;
    }
    
    @PostMapping("/temp")
    public String setTemp(@RequestParam int temp) {
        String ip = this.ip;
        if (temp < 2200 || temp > 6200) {
            return "Temperatura trebuie sa fie intre 2220K and 6200K.";
        }
        lightService.sendLightCommand(ip, true, null, temp);
        return "Temperatura setata " + temp + " pentru  " + ip;
    }

    @PostMapping("/mode")
    public String setMode(@RequestParam String mode) {
        try {
            LightMode m = LightMode.fromString(mode);
            lightService.applyMode(ip, m);
            return "Mod aplicat: " + m.name() + " to " + ip;
        } catch (IllegalArgumentException ex) {
            return "Mod invalid.";
        }
    }

    @GetMapping("/pilot")
    public WizPilotStateResponse getPilot() {
        return lightService.getPilot(ip);
    }

    public String getIp() {
        return ip;
    }


}