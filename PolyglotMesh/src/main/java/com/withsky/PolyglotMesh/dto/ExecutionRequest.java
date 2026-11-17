
package com.withsky.PolyglotMesh.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ExecutionRequest(

        @NotBlank(message = "Language is required")
        @Pattern(
                regexp = "(?i)python|javascript",
                message = "Supported languages: python, javascript"
        )
        String language,

        @NotBlank(message = "Code cannot be empty")
        @Size(
                max = 20000,
                message = "Code exceeds maximum size"
        )
        String code

) {
}
