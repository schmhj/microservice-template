package com.schmhj.microservice.adapter.in.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class HomeController {

    @GetMapping("/")
    public RedirectView home() {
        return new RedirectView("/actuator/health/liveness");
    }

}
