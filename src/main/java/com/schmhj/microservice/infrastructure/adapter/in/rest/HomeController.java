package com.schmhj.microservice.infrastructure.adapter.in.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@Tag(name = "Home", description = "Home Controller")
public class HomeController {

    @Hidden
    @GetMapping("/")
    public RedirectView home() {
        log.info("Home Controller");
        return new RedirectView("/swagger-ui/index.html");
    }

    @GetMapping("/test/hello")
    public String hello() {
        log.info("Hello Controller");
        return "Hello World";
    }

}
