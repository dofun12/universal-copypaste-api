package org.lemanoman.copypaste.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostMessageRequest(
        @NotBlank(message = "must not be blank")
        @Size(max = 20_000, message = "must be at most 20000 characters")
        String content
) {
}
