package pe.edu.clinica.sonrisa_andina.api.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "El usuario es obligatorio.") String username,
        @NotBlank(message = "La contrasena es obligatoria.") String password) {
}
