package pe.edu.clinica.sonrisa_andina.api.dto;

import java.time.LocalDate;
import java.util.List;

public record DisponibilidadResponse(
        Long odontologoId,
        String odontologo,
        Long servicioId,
        String servicio,
        int duracionMin,
        LocalDate fecha,
        List<String> horarios) {
}
