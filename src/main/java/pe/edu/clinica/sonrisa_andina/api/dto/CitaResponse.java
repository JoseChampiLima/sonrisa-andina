package pe.edu.clinica.sonrisa_andina.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import pe.edu.clinica.sonrisa_andina.domain.Cita;

public record CitaResponse(
        Long id,
        Long idPaciente,
        String paciente,
        Long idOdontologo,
        String odontologo,
        Long idServicio,
        String servicio,
        LocalDateTime inicio,
        LocalDateTime fin,
        int duracionMin,
        BigDecimal precio,
        String estado,
        String motivo) {

    public static CitaResponse de(Cita cita) {
        return new CitaResponse(cita.getId(), cita.getPaciente().getId(),
                cita.getPaciente().getNombreCompleto(), cita.getOdontologo().getId(),
                cita.getOdontologo().getNombreCompleto(), cita.getServicio().getId(),
                cita.getServicio().getNombre(), cita.getInicio(), cita.getFin(),
                cita.getServicio().getDuracionMin(), cita.getServicio().getPrecio(),
                cita.getEstado().name(), cita.getMotivo());
    }
}
