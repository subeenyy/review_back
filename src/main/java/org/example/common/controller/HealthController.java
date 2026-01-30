package org.example.common.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class HealthController {

    @GetMapping("/ping")
    public ResponseEntity<String> ping(@RequestHeader(value = "Host", required = false) String host) {
        log.info("Health check request received. Host: {}", host);
        return ResponseEntity.ok("pong");
    }
}
