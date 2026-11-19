package com.withsky.PolyglotMesh.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ExecutionRequest(

        @NotBlank(message = "Language is required")
        @Pattern(
                regexp = "python|javascript",
                message = "Only Python and JavaScript are supported"
        )
        String language,

        @NotBlank(message = "Code cannot be empty")
        @Size(
                max = 20000,
                message = "Maximum code size is 20000 characters"
        )
        String code

) {}
