package com.withsky.PolyglotMesh.controller;


import com.withsky.PolyglotMesh.dto.ExecutionRequest;
import com.withsky.PolyglotMesh.dto.ExecutionResponse;
import com.withsky.PolyglotMesh.service.PolyglotService;
import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class ExecutionController {

    private final PolyglotService polyglotService;

    public ExecutionController(PolyglotService polyglotService) {
        this.polyglotService = polyglotService;
    }

    @PostMapping(
            value = "/execute",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ExecutionResponse> executeCode(
            @Valid @RequestBody ExecutionRequest request
    ) {

        return Mono.fromCallable(
                () -> polyglotService.execute(request)
        ).subscribeOn(Schedulers.boundedElastic());

    }

    @GetMapping("/health")
    public Mono<String> health() {

        return Mono.just("PolyglotMesh Backend Running");

    }
}
