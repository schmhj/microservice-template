package com.schmhj.microservice.infrastructure.adapter.in.web;

import com.schmhj.microservice.infrastructure.adapter.in.rest.GlobalExceptionHandler;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.tracing.Tracer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest
@Import({ GlobalExceptionHandler.class, TestController.class })
@WithMockUser
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private Tracer tracer;

    @Test
    @DisplayName("should return bad request with violations when validation fails")
    void should_return_bad_request_with_violations_when_validation_fails() throws Exception {
        mockMvc.perform(post("/test/validation")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.violations").isArray())
                .andExpect(jsonPath("$.violations[0].field").value("name"))
                .andExpect(jsonPath("$.violations[0].message").value("must not be null"));
    }

    @Test
    @DisplayName("should return internal server error with message when generic exception occurs")
    void should_return_internal_server_error_with_message_when_generic_exception_occurs() throws Exception {
        mockMvc.perform(get("/test/exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Something went wrong"));
    }

    record TestRequest(@NotNull(message = "must not be null") String name) {
    }
}

@RestController
class TestController {

    @PostMapping("/test/validation")
    void validation(@Valid @RequestBody GlobalExceptionHandlerTest.TestRequest request) {
    }

    @GetMapping("/test/exception")
    void exception() {
        throw new RuntimeException("Something went wrong");
    }
}
