package pe.edu.clinica.sonrisa_andina.api.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public record CitaRequest(
        Long idPaciente,
        @NotNull(message = "El odontologo es obligatorio.") Long idOdontologo,
        @NotNull(message = "El servicio es obligatorio.") Long idServicio,
        @NotNull(message = "La fecha y hora de inicio son obligatorias.") LocalDateTime inicio,
        String motivo) {
}
