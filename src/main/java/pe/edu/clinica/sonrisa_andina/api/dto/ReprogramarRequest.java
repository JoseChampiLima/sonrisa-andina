package pe.edu.clinica.sonrisa_andina.api.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public record ReprogramarRequest(
        @NotNull(message = "El nuevo inicio es obligatorio.") LocalDateTime inicio) {
}
