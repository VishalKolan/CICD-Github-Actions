package com.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

    @GetMapping("/")
    public String home() {
        return "App is running ✅";
    }

    @GetMapping("/status")
    public String status() {
        return "OK";
    }
}
