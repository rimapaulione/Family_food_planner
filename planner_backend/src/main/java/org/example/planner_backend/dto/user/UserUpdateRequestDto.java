package org.example.planner_backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequestDto(

        @NotBlank @Size(max = 100)
        String displayName,

        @Size(max = 512)
        String avatarUrl
) {
}
